package projectZoom.connector.box.api

import play.api.libs.json._


case class BoxMiniUser(id: String, name: String, login: String)

object BoxMiniUser extends Function3[String, String, String, BoxMiniUser]{
  implicit val BoxMiniUserFormat: Format[BoxMiniUser] = Json.format[BoxMiniUser]
}

//incomplete
case class BoxUser(
    id: String,
    name: String,
    login: String,
    created_at: String,
    modified_at: String,
    role: String,
    status: String,
    job_title: String,
    phone: String
    )

object BoxUser extends Function9[String, String, String, String, String, String, String, String, String, BoxUser]{
  implicit val BoxUserFormat: Format[BoxUser] = Json.format[BoxUser]
}