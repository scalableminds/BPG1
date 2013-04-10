package projectZoom.connector.Filemaker

import java.sql.DriverManager

case class FileMakerDBInfo(hostName: String, dbName: String, userName: String, password: String)

class FilemakerAPI(con: java.sql.Connection) {
  
}



object FilemakerAPI {
  
  val d = Class.forName("com.filemaker.jdbc.Driver").newInstance() // load Driver
  
  def create(info: FileMakerDBInfo) = {
    val con = DriverManager.getConnection(s"jdbc:filemaker://${info.hostName}/${info.dbName}", info.userName, info.password)
    con.setReadOnly(true)
    new FilemakerAPI(con)
  }
  
}