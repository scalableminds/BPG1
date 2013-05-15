object dominic {

  import play.api.libs.json.JsValue
  import play.api.libs.json._
  import play.api.libs.json.util._
  import play.api.libs.json.Reads._
  import play.api.libs.functional.syntax._

  /*
	val system = ActorSystem("mysystem")
	val thumbnailboy = system.actorOf(Props[ThumbnailActor], "thumbnailActor")
	thumbnailboy ! 5
	
  println("Welcome to the Scala worksheet")
  */

  val js = Json.obj(
    "expires" -> 223,
    "stay" -> true)
 
  val r = (__ \ 'expires).json.copyFrom((__ \ 'expires_in).json.pick[JsNumber].map{
      case JsNumber(n) => JsNumber(n + 4)
      })

  js.transform(r)
}