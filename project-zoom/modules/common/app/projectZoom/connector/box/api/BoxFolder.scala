package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._
import projectZoom.util.DateTimeHelper
import org.joda.time.DateTime
import Reads.pattern

trait BoxBaseFolder{
  val id: String
}

case class BoxMiniFolder(id: String, sequence_id: Option[String], name: String, realType: String) extends BoxMiniSource with BoxBaseFolder {
  def rename(name: String) = this.copy(name = name)
}

object BoxMiniFolder extends Function4[String, Option[String], String, String, BoxMiniFolder]{
  
  implicit val BoxMiniFolderAsSourceReads: Reads[BoxMiniSource] = (
      (__ \ "id").read[String] and
      (__ \ "sequence_id").readNullable[String] and 
      (__ \ "name").read[String] and
      (__ \ "type").read[String](pattern("folder".r)))(BoxMiniFolder.apply _)
  
  implicit val BoxMiniFolderReads: Reads[BoxMiniFolder] = (
      (__ \ "id").read[String] and
      (__ \ "sequence_id").readNullable[String] and 
      (__ \ "name").read[String] and
      (__ \ "type").read[String](pattern("folder".r)))(BoxMiniFolder.apply _)
}   

case class BoxFolder( 
    id: String,
    sequence_id: Option[String],
    etag: Option[String],
    name: String,
    created_at: Option[DateTime],
    modified_at: Option[DateTime],
    description: String,
    size: Int,
    path_collection: BoxPathCollection,
    created_by: BoxMiniUser,
    modified_by: BoxMiniUser,
    owned_by: BoxMiniUser,
    parent: Option[BoxMiniFolder],
    item_status: String,
    item_collection: Option[BoxItemCollection]
    ) extends BoxSource with BoxBaseFolder with BoxFileSystemElement{
  def pathString = path_collection.pathString
  def fullPathString = s"${path_collection.pathString}/$name"
  def path = path_collection.path
  def fullPath = path_collection.path :+ name
  
  def rename(name: String) = this.copy(name = name)
  def relocate(index: Int, name: String) = this.copy(path_collection = path_collection.relocate(index, name))
}

object BoxFolder extends Function15[String, Option[String], Option[String], String, Option[DateTime], Option[DateTime], String, Int, BoxPathCollection, BoxMiniUser, BoxMiniUser, BoxMiniUser, Option[BoxMiniFolder], String, Option[BoxItemCollection], BoxFolder]{
  import DateTimeHelper.BoxTimeStampReader
  
  implicit val BoxFolderReads: Reads[BoxFolder] = Json.reads[BoxFolder]
  
  val BoxFolderAsSourceReads: Reads[BoxSource] = 
    ((__ \ "id").read[String] and
    (__ \ "sequence_id").readNullable[String] and
    (__ \ "etag").readNullable[String] and
    (__ \ "name").read[String] and
    (__ \ "created_at").readNullable[DateTime] and
    (__ \ "modified_at").readNullable[DateTime] and
    (__ \ "description").read[String] and
    (__ \ "size").read[Int] and
    (__ \ "path_collection").read[BoxPathCollection] and
    (__ \ "created_by").read[BoxMiniUser] and
    (__ \ "modified_by").read[BoxMiniUser] and
    (__ \ "owned_by").read[BoxMiniUser] and
    (__ \ "parent").readNullable[BoxMiniFolder] and
    (__ \ "item_status").read[String] and
    (__ \ "item_collection").readNullable[BoxItemCollection])(BoxFolder.apply _)
}