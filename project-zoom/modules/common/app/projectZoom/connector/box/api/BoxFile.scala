package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import projectZoom.util.DateTimeHelper

trait BoxFileSystemElement {
  val id: String
  val name: String
  def path: List[String]
  def fullPath: List[String]
}

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
    created_at: DateTime,
    modified_at: DateTime,
    trashed_at: Option[DateTime],
    purged_at: Option[DateTime],
    content_created_at: DateTime,
    content_modified_at: DateTime,
    created_by: BoxMiniUser,
    modified_by: BoxMiniUser,
    owned_by: BoxMiniUser,
    parent: BoxMiniFolder,
    item_status: String
    ) extends BoxSource with BoxFileSystemElement{
  def pathString = path_collection.pathString
  def fullPathString = s"${path_collection.pathString}/$name"
  def path = path_collection.path
  def fullPath = path_collection.path :+ name
  
  def rename(name: String) = this.copy(name = name)
  def relocate(index: Int, name: String) = this.copy(path_collection = path_collection.relocate(index, name))
}

object BoxFile extends Function19[String, String, String, String, String, String, Int, BoxPathCollection, DateTime, DateTime, Option[DateTime], Option[DateTime], DateTime, DateTime, BoxMiniUser, BoxMiniUser, BoxMiniUser, BoxMiniFolder, String, BoxFile]{
  import DateTimeHelper.BoxTimeStampReader

  implicit val BoxFileAsSourceReads: Reads[BoxSource] = (
    (__ \ "id").read[String] and
    (__ \ "sequence_id").read[String] and
    (__ \ "etag").read[String] and
    (__ \ "sha1").read[String] and
    (__ \ "name").read[String] and 
    (__ \ "description").read[String] and
    (__ \ "size").read[Int] and
    (__ \ "path_collection").read[BoxPathCollection] and
    (__ \ "created_at").read[DateTime] and
    (__ \ "modified_at").read[DateTime] and
    (__ \ "trashed_at").readNullable[DateTime] and
    (__ \ "purged_at").readNullable[DateTime] and
    (__ \ "content_created_at").read[DateTime] and
    (__ \ "content_modified_at").read[DateTime] and
    (__ \ "created_by").read[BoxMiniUser] and
    (__ \ "modified_by").read[BoxMiniUser] and
    (__ \ "owned_by").read[BoxMiniUser] and
    (__ \ "parent").read[BoxMiniFolder] and
    (__ \ "item_status").read[String])(BoxFile.apply _)
}

