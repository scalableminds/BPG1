package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._
import projectZoom.util.DateTimeHelper
import org.joda.time.DateTime

//incomplete
case class BoxLock(
    id: String,
    created_by: BoxMiniUser,
    created_at: DateTime,
    file: BoxMiniFile,
    expires_at: DateTime,
    lock_type: String) extends BoxSource

object BoxLock extends Function6[String, BoxMiniUser, DateTime, BoxMiniFile, DateTime, String, BoxLock]{
  import DateTimeHelper.BoxTimeStampReader
  
  implicit val BoxLockAsSourceReads: Reads[BoxSource] =  (
    (__ \ "id").read[String] and
    (__ \ "created_by").read[BoxMiniUser] and
    (__ \ "created_at").read[DateTime] and
    (__ \ "file").read[BoxMiniFile] and
    (__ \ "expires_at").read[DateTime] and
    (__ \ "lock_type").read[String])(BoxLock.apply _)
}