package projectZoom.connector.incom

import projectZoom.util._
import play.api.libs.ws._
import projectZoom.connector.ConnectorSettings
import scala.collection.JavaConverters._
import scala.util.{Success, Failure}

class IncomAPI(private val username: String, private val password: String) extends PlayActorSystem with IncomCookies{
  
  val cookies = scala.collection.mutable.Map[String,String]()
  
  def init() {
    cookies.clear
    WS.url("http://dschool.incom.org").get() andThen {
      case Failure(exception) => println(exception)
      case Success(response) => 
        updateCookies(response)
        WS.url("http://dschool.incom.org/action/login").withHeaders(buildCookie).post(loginPostData)      
    } andThen {
      case Failure(exception) => println(exception)
      case Success(response) => 
        updateCookies(response)
    }
  }
  
  private val loginPostData = Map("email" -> Seq(username), "password" -> Seq(password))
  
  def buildCookie() = ("Cookie", cookies.foldLeft("")((accu, t) => s"$accu ${t._1}=${t._2};"))
  
  def isLoggedIn() = cookies.get(dschoolSessionId) match {
    case Some(id) => id != "deleted"
    case _ => false
  }  

  def updateCookies(response: Response) = {
    cookies ++= 
    response.getAHCResponse.getCookies.asScala.toList.map(c => (c.getName, c.getValue)).toMap
  }
 
}

object IncomAPI extends PlayConfig with ConnectorSettings {
  
  def create(userName: String, password: String) = {
    new IncomAPI(userName: String, password: String)
  }
}

trait IncomCookies {
  val sessionId = "PHPSESSID"
  val dschoolSessionId = "HPI_UID"
}