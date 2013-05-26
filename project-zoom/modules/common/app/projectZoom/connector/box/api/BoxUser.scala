package projectZoom.connector.box.api

import play.api.libs.json._


case class BoxUser(id: String, name: String, login: String)

object BoxUser extends Function3[String, String, String, BoxUser]{
  implicit val BoxUserFormat: Format[BoxUser] = Json.format[BoxUser]
}
