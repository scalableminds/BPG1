object thomas {
  import play.api.libs.ws.WS
  import scala.concurrent._
  import scala.concurrent.duration._
  import scala.util.matching.Regex
  import scala.collection.JavaConverters._
  import scala.collection.mutable.Map
  
  val x = scala.collection.mutable.Map[String,String]("aa" -> "bb")
                                                  //> x  : scala.collection.mutable.Map[String,String] = Map(aa -> bb)
  x.contains("bb")                                //> res0: Boolean = false
  
  
  type xy = Int
  
  val a: xy = 5                                   //> a  : thomas.xy = 5
  /*
  val landingRequest = WS.url("http://dschool.incom.org").get()
  val response = Await.result(landingRequest, 10 seconds)
  //val CookieRegEx = """([a-zA-Z0-9_\-]+=[a-zA-Z0-9_\-/]+)(;|$)""".r
  //val PHPSessionId = CookieRegEx.findAllIn(response.header("Set-Cookie").get).toList
  
  response.getAHCResponse.getCookies.asScala.toList.map(c => (c.getName, c.getValue))
  
  */
}