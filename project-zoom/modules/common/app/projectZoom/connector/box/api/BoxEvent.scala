package projectZoom.connector.box.api

import play.api.libs.json._
import org.joda.time.DateTime
import projectZoom.util.DateTimeHelper
import play.api.libs.functional.syntax._


case class BoxEvent(
    event_id: String, 
    event_type: String, 
    created_by: BoxMiniUser, 
    created_at: DateTime, 
    session_id: Option[String], 
    source: Option[BoxSource],
    json: JsValue)

object BoxEvent extends Function7[String, String, BoxMiniUser, DateTime, Option[String], Option[BoxSource], JsValue, BoxEvent]{
  import DateTimeHelper.BoxTimeStampReader
  
  implicit val BoxEventReads: Reads[BoxEvent] = (
    (__ \ "event_id").read[String] and 
    (__ \ "event_type").read[String] and 
    (__ \ "created_by").read[BoxMiniUser] and 
    (__ \ "created_at").read[DateTime] and 
    (__ \ "session_id").read[Option[String]] and 
    (__ \ "source").readNullable[BoxSource] and
    (__).read[JsValue])(BoxEvent.apply _)
}