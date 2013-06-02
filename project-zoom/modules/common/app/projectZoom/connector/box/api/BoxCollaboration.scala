package projectZoom.connector.box.api

import play.api.libs.json._

case class BoxCollaboration(id: String, 
    created_by: BoxMiniUser, 
    created_at: String, 
    accessible_by: BoxMiniUser, 
    role: String, 
    item: BoxMiniFolder)

object BoxCollaboration extends Function6[String, BoxMiniUser, String, BoxMiniUser, String, BoxMiniFolder, BoxCollaboration]{
  
  implicit val boxCollaborationFormat: Reads[BoxCollaboration] = Json.reads[BoxCollaboration]
}