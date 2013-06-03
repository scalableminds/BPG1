package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class BoxMiniFile(id: String, sequence_id: String, name: String) extends BoxMiniSource

object BoxMiniFile extends Function3[String, String, String, BoxMiniFile] {
  implicit val BoxMiniFileAsSourceReads: Reads[BoxMiniSource] = (
      (__ \ "id").read[String] and
      (__ \ "sequence_id").read[String] and 
      (__ \ "name").read[String])(BoxMiniFile.apply _)
      
  implicit val BoxMiniFileReads: Reads[BoxMiniFile] = Json.reads[BoxMiniFile]
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
    trashed_at: Option[String],
    purged_at: Option[String],
    content_created_at: String,
    content_modified_at: String,
    created_by: BoxMiniUser,
    modified_by: BoxMiniUser,
    owned_by: BoxMiniUser,
    parent: BoxMiniFolder,
    item_status: String
    ) extends BoxSource {
  val fullPath = s"${path_collection.path}/$name"
  val path = path_collection.path
}

object BoxFile extends Function19[String, String, String, String, String, String, Int, BoxPathCollection, String, String, Option[String], Option[String], String, String, BoxMiniUser, BoxMiniUser, BoxMiniUser, BoxMiniFolder, String, BoxFile]{
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
    (__ \ "trashed_at").readNullable[String] and
    (__ \ "purged_at").readNullable[String] and
    (__ \ "content_created_at").read[String] and
    (__ \ "content_modified_at").read[String] and
    (__ \ "created_by").read[BoxMiniUser] and
    (__ \ "modified_by").read[BoxMiniUser] and
    (__ \ "owned_by").read[BoxMiniUser] and
    (__ \ "parent").read[BoxMiniFolder] and
    (__ \ "item_status").read[String])(BoxFile.apply _)
}

