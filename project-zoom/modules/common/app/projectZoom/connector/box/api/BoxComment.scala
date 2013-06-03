package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class BoxItem(id: String, itemType: String)

object BoxItem{
  implicit val BoxItemReads: Reads[BoxItem] = (
      (__ \ "id").read[String] and
      (__ \ "type").read[String])(BoxItem.apply _)
}

case class BoxComment(
    id: String,
    is_reply_comment: Boolean,
    message: String,
    created_by: BoxMiniUser,
    created_at: String,
    item: BoxItem,
    modified_at: String
    ) extends BoxSource
    
object BoxComment{
  implicit val BoxCommentAsSourceReads: Reads[BoxSource] = (
    (__ \ "id").read[String] and
    (__ \ "is_reply_comment").read[Boolean] and
    (__ \ "message").read[String] and
    (__ \ "created_by").read[BoxMiniUser] and
    (__ \ "created_at").read[String] and
    (__ \ "item").read[BoxItem] and
    (__ \ "modified_at").read[String])(BoxComment.apply _)
}