package projectZoom.connector.incom

import projectZoom.util._
import play.api.libs.ws._
import projectZoom.connector.ConnectorSettings
import scala.util.{Success, Failure}
import play.api.Logger
import projectZoom.util.CookieJarType._
import play.api.libs.concurrent.Execution.Implicits._


trait IncomCookieHelper extends CookieHelper{

  val sessionId = "PHPSESSID"
  val dschoolSessionId = "HPI_UID"
  
  def isLoggedIn(cookies: CookieJar) = cookies.contains(sessionId) && 
    cookies.contains(dschoolSessionId) && 
    cookies(dschoolSessionId) != "deleted"
}

class IncomAPI(cookies: CookieJar) extends PlayActorSystem with IncomCookieHelper{
  def extractPosts() = {}
}

object IncomAPI extends PlayConfig with IncomCookieHelper{
  
  def create(userName: String, password: String): Option[IncomAPI] = {
    val cookies: CookieJar = scala.collection.mutable.Map[String, String]()
    val loginPostData = Map("email" -> Seq(userName), "password" -> Seq(password))
    
    (WS.url("http://dschool.incom.org").get() andThen {
      case Failure(exception) => Logger.error(exception.getMessage())
      case Success(response) => 
        cookies ++ extractCookieMap(response)
        WS.url("http://dschool.incom.org/action/login").withHeaders(buildCookie(cookies)).post(loginPostData)      
    } andThen {
      case Failure(exception) => Logger.error(exception.getMessage())
      case Success(response) => 
        cookies ++ extractCookieMap(response)
    })
   if(isLoggedIn(cookies)) Some(new IncomAPI(cookies)) else None
  }
}