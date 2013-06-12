package projectZoom.connector.box.api

import play.api.libs.json._
import org.joda.time.DateTime
import projectZoom.util.DateTimeHelper

case class BoxEvent(
    event_id: String, 
    event_type: String, 
    created_by: BoxMiniUser, 
    created_at: DateTime, 
    session_id: Option[String], 
    source: Option[BoxSource])

object BoxEvent extends Function6[String, String, BoxMiniUser, DateTime, Option[String], Option[BoxSource], BoxEvent]{
  import DateTimeHelper.BoxTimeStampReader
  implicit val BoxEventReads: Reads[BoxEvent] = Json.reads[BoxEvent]
}