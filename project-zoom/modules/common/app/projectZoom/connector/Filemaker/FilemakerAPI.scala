package projectZoom.connector.Filemaker

import java.sql.{ DriverManager, ResultSet }
import models.{ Profile, ProjectLike, Participant }
import scala.collection.mutable.{ ListBuffer, Set }
import play.api.Logger
import scala.util.Try

case class PartialProject(year: String, season: String, length: String, participants: ListBuffer[Participant], tags: Set[String])

case class FileMakerDBInfo(hostName: String, dbName: String, userName: String, password: String)

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
    val project = rs.getString("project")
    val className = rs.getString("class")
    if (length <= 6) {
      if(project != "null") project else "?" + " - " +
      (if(className != "null") className else "?")
    }
    else {
      project
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
    val rs = queryDB( "select \"E-Mail 1\" as email, %s, %s, \"%d-W-Project\" as project, \"%d-W-Class\" as class from dmiA where %s and %s".format(dschoolTags, projectTags(length), length, length, userHasEmail, userHasTakenClasses(length)))
    
    while (rs.next) {
      val dschoolTags = extractDschoolTags(rs)
      val email = rs.getString("email")
      val projectTags = extractProjectTags(rs, length)
      dschoolTags.find(tag => tag.contains(s"Student_${lengthTrackMapping(length)}")).foreach{ tag => 
        val season = tag.split("_")(2)
        val year = tag.split("_")(3)
        val projectName = createProjectName(rs, length)
        projects.get(projectName) match {
          case Some(PartialProject(_, _, _, l, t)) =>
            l.append(Participant("student", email))
            t ++ projectTags
          case None => projects += (projectName -> PartialProject(year, season, s"{length}W", ListBuffer[Participant](Participant("student", email)), Set() ++ projectTags))
        }
      }   
    }
    projects
  }

  def extractProjects(): List[ProjectLike] = {
    val projects = List(3,6,12).map(length => extractProjects(length))
    projects.flatMap{projectMap => 
      projectMap.toList.map{p => 
        ProjectLike(p._1, p._2.participants.toList, p._2.season, p._2.year, p._2.length, p._2.tags.toList)
      }
    }
  }
}

object FilemakerAPI {

  val d = Class.forName("com.filemaker.jdbc.Driver").newInstance() // load Driver

  def create(info: FileMakerDBInfo) = {
    Try {
      val con = DriverManager.getConnection(s"jdbc:filemaker://${info.hostName}/${info.dbName}", info.userName, info.password)
      con.setReadOnly(true)
      new FilemakerAPI(con)
    }
  }

}