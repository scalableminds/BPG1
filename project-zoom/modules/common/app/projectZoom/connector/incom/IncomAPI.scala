package projectZoom.connector.incom

import projectZoom.util._
import play.api.libs.ws._
import projectZoom.connector.ConnectorSettings
import scala.util.{Success, Failure}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._

class IncomAPI(cookies: CookieJar) extends PlayActorSystem {
  def extractPosts() = {}
  
}

object IncomAPI extends PlayConfig {
  
  val sessionId = "PHPSESSID"
  val dschoolSessionId = "HPI_UID"
    
  def isLoggedIn(cookieJar: CookieJar) = {
    val cookies = cookieJar.getCookieMap
    cookies.contains(sessionId) && 
    cookies.contains(dschoolSessionId) && 
    cookies(dschoolSessionId) != "deleted"
  }
  
  def create(userName: String, password: String): Option[IncomAPI] = {
    val cookieJar = new CookieJar
    val loginPostData = Map("email" -> Seq(userName), "password" -> Seq(password))
    
    (WS.url("http://dschool.incom.org").get() andThen {
      case Failure(exception) => Logger.error(exception.getMessage())
      case Success(response) => 
        cookieJar.saveCookieFromResponse(response)
        WS.url("http://dschool.incom.org/action/login").withHeaders(cookieJar.buildCookieHeader).post(loginPostData)      
    } andThen {
      case Failure(exception) => Logger.error(exception.getMessage())
      case Success(response) => 
        cookieJar.saveCookieFromResponse(response)
    })
   if(isLoggedIn(cookieJar)) Some(new IncomAPI(cookieJar)) else None
  }
}