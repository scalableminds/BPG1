package projectZoom.connector.Filemaker

import java.sql.{DriverManager, ResultSet}
import models.{Profile, ProjectLike, Participant}
import scala.collection.mutable.ListBuffer
import play.api.Logger

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
    val rs = statement.executeQuery("select \"E-Mail 1\" as email, %s, \"12-W-Project\" from dmiA where \"12-W-Project\" is not null".format(tagfield))
    val projects = scala.collection.mutable.Map[String, ListBuffer[Participant]]()
    
    while(rs.next) {
      val userTags = extractTags(rs)
      if (userTags.exists(tag => tag.contains("Student_AT")))
        projects.get(rs.getString("12-W-Project")) match {
        case Some(l: ListBuffer[Participant]) => l.append(Participant("student", rs.getString("email")))
        case None => projects += (rs.getString("12-W-Project") -> new ListBuffer[Participant])
        }
      }
    
    projects.toList.map(p => ProjectLike(p._1, (p._2).toList, List()))
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