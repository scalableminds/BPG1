package projectZoom.connector.Filemaker

import java.sql.{DriverManager, ResultSet}
import models.Profile

case class FileMakerDBInfo(hostName: String, dbName: String, userName: String, password: String)

class FilemakerAPI(con: java.sql.Connection) {
  
  def extractStudents(): List[Profile] = {
    
    def extractTags(rs: ResultSet) = {
      List[String](rs.getString("d1"), rs.getString("d2"), rs.getString("d3"), rs.getString("d4"), rs.getString("d5"), rs.getString("d6")).
      filter(_ != null)
    }
  
    val statement = con.createStatement()           //> statement  : java.sql.Statement = com.filemaker.jdbc3.J3Statement@6a340101
    val rs = statement.executeQuery("select Vorname, Name, \"E-Mail 1\" as email, dschool[1] as d1, dschool[2] as d2, dschool[3] as d3, dschool[4] as d4, dschool[5] as d5, dschool[6] as d6 from dmiA")
    val l = scala.collection.mutable.ListBuffer[Profile]()
  
    while (rs.next) {
    if(extractTags(rs).exists(tag => tag.contains("Student")))
      l.append(Profile(rs.getString("Vorname"), rs.getString("Name"), rs.getString("email")))
    }
    l.toList
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