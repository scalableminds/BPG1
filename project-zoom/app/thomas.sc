object thomas {
  import play.api.libs.ws.WS
  import scala.concurrent._
  import scala.concurrent.duration._
  import scala.util.matching.Regex
  import scala.collection.JavaConverters._
  
  val landingRequest = WS.url("http://dschool.incom.org").get()
                                                  //> landingRequest  : scala.concurrent.Future[play.api.libs.ws.Response] = scala
                                                  //| .concurrent.impl.Promise$DefaultPromise@786c1a82
  val response = Await.result(landingRequest, 10 seconds)
                                                  //> response  : play.api.libs.ws.Response = Response(com.ning.http.client.provid
                                                  //| ers.netty.NettyResponse@548997d1)
  //val CookieRegEx = """([a-zA-Z0-9_\-]+=[a-zA-Z0-9_\-/]+)(;|$)""".r
  //val PHPSessionId = CookieRegEx.findAllIn(response.header("Set-Cookie").get).toList
  
  response.getAHCResponse.getCookies.asScala.toList.map(c => (c.getName, c.getValue))
                                                  //> res0: List[(String, String)] = List((PHPSESSID,h54gqhgs5f9pso0061n8v26bp1rgb
  
                                                  //| 5ts))|
}