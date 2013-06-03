package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class BoxCollaboration(id: String, 
    created_by: BoxMiniUser, 
    created_at: String, 
    accessible_by: Option[BoxMiniUser], 
    role: String, 
    item: BoxMiniFolder) extends BoxSource

object BoxCollaboration extends Function6[String, BoxMiniUser, String, Option[BoxMiniUser], String, BoxMiniFolder, BoxCollaboration]{
  
  implicit val boxCollaborationReads: Reads[BoxCollaboration] = Json.reads[BoxCollaboration]
  
  implicit val boxCollaborationAsSourceReads: Reads[BoxSource] = (
      (__ \ "id").read[String] and 
      (__ \ "created_by").read[BoxMiniUser] and 
      (__ \ "created_at").read[String] and 
      (__ \ "accessible_by").readNullable[BoxMiniUser] and 
      (__ \ "role").read[String] and 
      (__ \ "item").read[BoxMiniFolder])(BoxCollaboration.apply _)
}