package projectZoom.connector.Filemaker

import java.sql.{ DriverManager, ResultSet }
import models.{ Profile, ProjectLike, Participant }
import scala.collection.mutable.{ ListBuffer, Set }
import play.api.Logger
import scala.util.{ Try, Success, Failure }
import projectZoom.util.PlayConfig
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import projectZoom.util.SSH

case class PartialProject(year: String, season: String, length: String, participants: ListBuffer[Participant], tags: Set[String])

class FilemakerAPI(con: java.sql.Connection) {

  val dschoolTags = "dschool[1] as d1, dschool[2] as d2, dschool[3] as d3, dschool[4] as d4, dschool[5] as d5, dschool[6] as d6"
  val userHasEmail = "\"E-Mail 1\" is not null"
  def userHasTakenClasses(length: Int) = "(\"%d-W-Class\" is not null or \"%d-W-Project\" is not null)".format(length, length)
  def projectTags(length: Int) = "\"%d-W-Tag\"[1] as tag%dW1, \"%d-W-Tag\"[2] as tag%dW2, \"%d-W-Tag\"[3] as tag%dW3".format(length, length, length, length, length, length)

  val lengthTrackMapping = Map[Int, String](3 -> "BT", 6 -> "BT", 12 -> "AT")

  def extractDschoolTags(rs: ResultSet) = {
    (for (i <- 1 to 6) yield rs.getString(s"d$i")).filter(_ != null)
  }

  def extractProjectTags(rs: ResultSet, nrOfWeeks: Int) = {
    (for (i <- 1 to 3) yield rs.getString(s"tag${nrOfWeeks}W$i")).filter(_ != null)
  }

  def createProjectName(rs: ResultSet, length: Int) = {
    val project = Option(rs.getString("project"))
    val className = Option(rs.getString("class"))
    if (length <= 6) {
      val teamSuffix = if (project.isDefined) " - " + project.get
      className.getOrElse("?") + teamSuffix
    } else {
      project.getOrElse("?")
    }
  }

  def extractStudents(): List[Profile] = {
    val rs = queryDB("select Vorname, Name, \"E-Mail 1\" as email, %s from dmiA where %s".format(dschoolTags, userHasEmail))
    val l = scala.collection.mutable.ListBuffer[Profile]()

    while (rs.next) {
      if (extractDschoolTags(rs).exists(tag => tag.contains("Student")))
        l.append(Profile(rs.getString("Vorname"), rs.getString("Name"), rs.getString("email")))
    }
    l.toList
  }

  def queryDB(queryString: String) = {
    val statement = con.createStatement()
    statement.executeQuery(queryString)
  }

  def extractProjects(length: Int) = {
    val projects = scala.collection.mutable.Map[String, PartialProject]()
    val lengthW = s"${length}W"
    val rs = queryDB("select \"E-Mail 1\" as email, %s, %s, \"%d-W-Project\" as project, \"%d-W-Class\" as class from dmiA where %s and %s".format(dschoolTags, projectTags(length), length, length, userHasEmail, userHasTakenClasses(length)))

    while (rs.next) {
      val dschoolTags = extractDschoolTags(rs)
      val email = rs.getString("email").toLowerCase
      val projectTags = extractProjectTags(rs, length)
      dschoolTags.find(tag => tag.contains(s"Student_${lengthTrackMapping(length)}")).foreach { tag =>
        val season = tag.split("_")(2)
        val year = tag.split("_")(3)
        val projectName = createProjectName(rs, length)
        projects.get(projectName) match {
          case Some(PartialProject(_, _, _, l, t)) =>
            l.append(Participant("student", email))
            t ++ projectTags
          case None => projects += (projectName -> PartialProject(year, season, s"${length}W", ListBuffer[Participant](Participant("student", email)), Set() ++ projectTags))
        }
      }
    }
    //Logger.debug(projects.toString)
    projects
  }

  def extractProjects(): List[ProjectLike] = {
    val projects = List(3, 6, 12).map(length => extractProjects(length))
    projects.flatMap { projectMap =>
      projectMap.toList.map { p =>
        ProjectLike(p._1, p._2.participants.toList, p._2.season, p._2.year, p._2.length, p._2.tags.toList)
      }
    }
  }
}

object FilemakerAPI extends PlayConfig {

  val d = Class.forName("com.filemaker.jdbc.Driver").newInstance() // load Driver

  def connect(host: String, dbName: String, user: String, password: String): Option[FilemakerAPI] =
    Try {
      val con = DriverManager.getConnection(s"jdbc:filemaker://$host/$dbName", user, password)
      con.setReadOnly(true)
      new FilemakerAPI(con)
    } match {
      case Success(api) => Some(api)
      case Failure(err) =>
        Logger.error(err.toString)
        None
    }

  def buildConnection: Option[FilemakerAPI] = {
    for {
      host <- config.getString("filemaker.host")
      dbName <- config.getString("filemaker.db")
      user <- config.getString("filemaker.user")
      password <- config.getString("filemaker.password")
      api <- connect(host, dbName, user, password)
    } yield {
      api
    }
  }

  def create: Option[FilemakerAPI] = {
    if (config.getBoolean("filemaker.ssh.enabled") getOrElse false) {
      (for {
        host <- config.getString("filemaker.ssh.host")
        user <- config.getString("filemaker.ssh.user")
        password <- config.getString("filemaker.ssh.password")
      } yield {
        val f = Future(SSH.createTunnel(host, 22, user, password, 2399, "127.0.0.1", 2399)).map { created =>
          if (created) Logger.info("created SSH Tunnel to filemaker")
          else Logger.info("Failed to create SSH Tunnel, maybe it's already open")
        }
        Try{
          Await.ready(f, 10 seconds)
        } match {
          case Success(_) => 
          case Failure(err) => Logger.error("Timeout for tunnelcreation")
        }
      }).flatMap(_ => buildConnection)
    } else buildConnection
  }

}