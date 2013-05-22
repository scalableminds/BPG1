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
  val userHasTakenClasses = "(\"3-W-Class\" is not null or \"3-W-Class\" is not null or \"6-W-Project\" is not null or \"6-W-Class\" is not null or \"12-W-Class\" is not null or \"12-W-Project\" is not null)"
  val projectTags = "\"3-W-Tag\"[1] as tag3W1, \"3-W-Tag\"[2] as tag3W2, \"3-W-Tag\"[3] as tag3W3, \"6-W-Tag\"[1] as tag6W1, \"6-W-Tag\"[2] as tag6W2, \"6-W-Tag\"[3] as tag6W3, \"12-W-Tag\"[1] as tag12W1, \"12-W-Tag\"[2] as tag12W2, \"12-W-Tag\"[3] as tag12W3"

  def extractDschoolTags(rs: ResultSet) = {
    (for (i <- 1 to 6) yield rs.getString(s"d$i")).filter(_ != null)
  }

  def extractProjectTags(rs: ResultSet, nrOfWeeks: String) = {
    (for (i <- 1 to 3) yield rs.getString(s"tag${nrOfWeeks}W$i")).filter(_ != null)
  }

  def extractStudents(): List[Profile] = {
    val statement = con.createStatement()
    val rs = statement.executeQuery("select Vorname, Name, \"E-Mail 1\" as email, %s from dmiA where %s".format(dschoolTags, userHasEmail))
    val l = scala.collection.mutable.ListBuffer[Profile]()

    while (rs.next) {
      if (extractDschoolTags(rs).exists(tag => tag.contains("Student")))
        l.append(Profile(rs.getString("Vorname"), rs.getString("Name"), rs.getString("email")))
    }
    l.toList
  }

  def extractProjects(): List[ProjectLike] = {
    val statement = con.createStatement()
    val rs = statement.executeQuery("select \"E-Mail 1\" as email, %s, %s, \"12-W-Project\", \"3-W-Project\", \"3-W-Class\", \"6-W-Project\", \"6-W-Class\" from dmiA where %s and %s".format(dschoolTags, projectTags, userHasEmail, userHasTakenClasses))
    val projects = scala.collection.mutable.Map[String, PartialProject]()

    while (rs.next) {
      val dschoolTags = extractDschoolTags(rs)
      val email = rs.getString("email")
      val tags3W = extractProjectTags(rs, "3")
      val tags6W = extractProjectTags(rs, "6")
      val tags12W = extractProjectTags(rs, "12")
      dschoolTags.find(tag => tag.contains("Student_AT")).foreach { tag =>
        val season = tag.split("_")(2)
        val year = tag.split("_")(3)
        val length = "12W"
        val projectName = rs.getString("12-W-Project")
        projects.get(projectName) match {
          case Some(PartialProject(_, _, _, l, t)) =>
            l.append(Participant("student", email));
            t ++ tags12W
          case None => projects += (projectName -> PartialProject(year, season, length, ListBuffer[Participant](Participant("student", email)), Set() ++ tags12W))
        }
      }
      dschoolTags.find(tag => tag.contains("Student_BT")).foreach { tag =>
        val season = tag.split("_")(2)
        val year = tag.split("_")(3)
        val threeWeekProject = s"${rs.getString("3-W-Class")} - ${rs.getString("3-W-Project")}"
        val sixWeekProject = s"${rs.getString("6-W-Class")} - ${rs.getString("6-W-Project")}"

        projects.get(threeWeekProject) match {
          case Some(PartialProject(_, _, _, l, t)) =>
            l.append(Participant("student", rs.getString("email")))
            t ++ tags3W
          case None => projects += (threeWeekProject -> PartialProject(year, season, "3W", ListBuffer[Participant](Participant("student", email)), Set() ++ tags3W))
        }

        projects.get(sixWeekProject) match {
          case Some(PartialProject(_, _, _, l, t)) =>
            l.append(Participant("student", rs.getString("email")))
            t ++ tags6W
          case None => projects += (sixWeekProject -> PartialProject(year, season, "6W", ListBuffer[Participant](Participant("student", email)), Set() ++ tags6W))
        }
      }

    }
    projects.toList.map(p => ProjectLike(p._1, p._2.participants.toList, p._2.year, p._2.season, p._2.length, p._2.tags.toList))
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