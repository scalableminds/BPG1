package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class BoxMiniFile(id: String, sequence_id: String, name: String) extends BoxMiniSource

object BoxMiniFile extends Function3[String, String, String, BoxMiniFile] {
  implicit val BoxMiniFileAsSourceReads: Reads[BoxMiniSource] = (
      (__ \ "id").read[String] and
      (__ \ "sequence_id").read[String] and 
      (__ \ "name").read[String])(BoxMiniFile.apply _)
}

case class BoxFile(
    id: String,
    sequence_id: String,
    etag: String,
    sha1: String,
    name: String,
    description: String,
    size: Int,
    path_collection: BoxPathCollection,
    created_at: String,
    modified_at: String,
    trashed_at: String,
    purged_at: String,
    content_created_at: String,
    content_modified_at: String,
    created_by: BoxUser,
    modified_by: BoxUser,
    owned_by: BoxUser,
    parent: BoxMiniFolder,
    item_status: String
    ) extends BoxSource {
  val fullPath = s"${path_collection.path}/$name"
  val path = path_collection.path
}

object BoxFile extends Function19[String, String, String, String, String, String, Int, BoxPathCollection, String, String, String, String, String, String, BoxUser, BoxUser, BoxUser, BoxMiniFolder, String, BoxFile]{
  implicit val BoxFileAsSourceReads: Reads[BoxSource] = (
    (__ \ "id").read[String] and
    (__ \ "sequence_id").read[String] and
    (__ \ "etag").read[String] and
    (__ \ "sha1").read[String] and
    (__ \ "name").read[String] and 
    (__ \ "description").read[String] and
    (__ \ "size").read[Int] and
    (__ \ "path_collection").read[BoxPathCollection] and
    (__ \ "created_at").read[String] and
    (__ \ "modified_at").read[String] and
    (__ \ "trashed_at").read[String] and
    (__ \ "purged_at").read[String] and
    (__ \ "content_created_at").read[String] and
    (__ \ "content_modified_at").read[String] and
    (__ \ "created_by").read[BoxUser] and
    (__ \ "modified_by").read[BoxUser] and
    (__ \ "owned_by").read[BoxUser] and
    (__ \ "parent").read[BoxMiniFolder] and
    (__ \ "item_status").read[String])(BoxFile.apply _)
}

