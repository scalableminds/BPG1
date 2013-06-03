package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

//incomplete
case class BoxLock(
    id: String,
    created_by: BoxMiniUser,
    created_at: String,
    file: BoxMiniFile,
    expires_at: String,
    lock_type: String) extends BoxSource

object BoxLock extends Function6[String, BoxMiniUser, String, BoxMiniFile, String, String, BoxLock]{
  implicit val BoxLockAsSourceReads: Reads[BoxSource] =  (
    (__ \ "id").read[String] and
    (__ \ "created_by").read[BoxMiniUser] and
    (__ \ "created_at").read[String] and
    (__ \ "file").read[BoxMiniFile] and
    (__ \ "expires_at").read[String] and
    (__ \ "lock_type").read[String])(BoxLock.apply _)
}