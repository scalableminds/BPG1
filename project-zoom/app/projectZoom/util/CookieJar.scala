package projectZoom.util

import akka.agent._
import play.api.libs.ws._
import scala.collection.JavaConverters._

class CookieJar extends PlayActorSystem {
  
  private val jar = Agent(Map[String,String]())
  
  def saveCookieFromResponse(r: Response) {
    jar send (_ ++ r.getAHCResponse.getCookies.asScala.toList.map(c => (c.getName, c.getValue)).toMap)
  }
  
  def buildCookieHeader = {
    ("Cookie", jar().foldLeft("")((accu, t) => s"$accu ${t._1}=${t._2};"))
  }
  
  def getCookieMap = jar()
  
}