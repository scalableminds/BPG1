package projectZoom.connector.Filemaker

import java.sql.{DriverManager, ResultSet}
import models.{Profile, ProjectLike, Participant}
import scala.collection.mutable.ListBuffer
import play.api.Logger

case class PartialProject(year: String, season: String, length: String, participants: ListBuffer[Participant])

case class FileMakerDBInfo(hostName: String, dbName: String, userName: String, password: String)

class FilemakerAPI(con: java.sql.Connection) {
  
  val tagfield = "dschool[1] as d1, dschool[2] as d2, dschool[3] as d3, dschool[4] as d4, dschool[5] as d5, dschool[6] as d6"
  
  def extractTags(rs: ResultSet) = {
      List[String](rs.getString("d1"), rs.getString("d2"), rs.getString("d3"), rs.getString("d4"), rs.getString("d5"), rs.getString("d6")).
      filter(_ != null)
  }
  
  def extractStudents(): List[Profile] = {
  
    val statement = con.createStatement()
    val rs = statement.executeQuery("select Vorname, Name, \"E-Mail 1\" as email, %s from dmiA".format(tagfield))
    val l = scala.collection.mutable.ListBuffer[Profile]()
  
    while (rs.next) {
    if(extractTags(rs).exists(tag => tag.contains("Student")))
      l.append(Profile(rs.getString("Vorname"), rs.getString("Name"), rs.getString("email")))
    }
    l.toList
  }
  
  def extractProjects(): List[ProjectLike] = {
    val statement = con.createStatement()
    val rs = statement.executeQuery("select \"E-Mail 1\" as email, %s, \"12-W-Project\", \"3-W-Project\", \"3-W-Class\", \"6-W-Project\", \"6-W-Class\" from dmiA where \"E-Mail 1\" is not null".format(tagfield))
    val projects = scala.collection.mutable.Map[String, PartialProject]()
    
    while(rs.next) {
      val userTags = extractTags(rs)
      val email = rs.getString("email")
      userTags.find(tag => tag.contains("Student_AT")).foreach{
        tag =>
        val season = tag.split("_")(2)
        val year = tag.split("_")(3)
        val length = "12W"
        val projectName = rs.getString("12-W-Project")
        projects.get(projectName) match {
          case Some(PartialProject(_, _, _, l)) => l.append(Participant("student", email)); //Logger.info(s"added $email to $projectName")
          case None => projects += (projectName -> PartialProject(year, season, length, ListBuffer[Participant](Participant("student", email))))
        }   
      }
      userTags.find(tag => tag.contains("Student_BT")).foreach{
        tag =>
        val season = tag.split("_")(2)
        val year = tag.split("_")(3)
        val threeWeekProject = s"${rs.getString("3-W-Class")} - ${rs.getString("3-W-Project")}"
        val sixWeekProject = s"${rs.getString("6-W-Class")} - ${rs.getString("6-W-Project")}"
        
        List((threeWeekProject, "3W"), (sixWeekProject, "6W")).foreach{ t =>
          val projectName = t._1
          val length = t._2
          projects.get(projectName) match {
            case Some(PartialProject(_, _, _, l)) => l.append(Participant("student", rs.getString("email"))); //Logger.info(s"added $email to $projectName")
            case None => projects += (projectName -> PartialProject(year, season, length, ListBuffer[Participant](Participant("student", rs.getString("email")))))
          }
        }
      }
        
    }
    
    projects.toList.map(p => ProjectLike(p._1, p._2.participants.toList, p._2.year, p._2.season, p._2.length , List()))
  }
  }



object FilemakerAPI {
  
  val d = Class.forName("com.filemaker.jdbc.Driver").newInstance() // load Driver
  
  def create(info: FileMakerDBInfo) = {
    val con = DriverManager.getConnection(s"jdbc:filemaker://${info.hostName}/${info.dbName}", info.userName, info.password)
    con.setReadOnly(true)
    new FilemakerAPI(con)
  }
  
}