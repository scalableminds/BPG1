package projectZoom.util

import play.api.libs.ws._
import scala.collection.JavaConverters._

object CookieJarType{
  type CookieJar = scala.collection.mutable.Map[String, String]
}

trait CookieHelper {
  import CookieJarType._
  
  def extractCookieMap(response: Response) = 
    response.getAHCResponse.getCookies.asScala.toList.map(c => (c.getName, c.getValue)).toMap
    
  def buildCookie(cookies: CookieJar) = ("Cookie", cookies.foldLeft("")((accu, t) => s"$accu ${t._1}=${t._2};"))
}