package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class BoxFolder(
    id: String, 
    sequence_id: String,
    name: String,
    pathCollection: Option[BoxPathCollection]) extends BoxSource {

}

object BoxFolder extends Function4[String, String, String, Option[BoxPathCollection], BoxFolder]{
  val BoxFolderReads: Reads[BoxSource] = 
    ((__ \ "id").read[String] and 
     (__ \ "sequence_id").read[String] and
     (__ \ "name").read[String] and
     (__ \ "path_collection").readNullable[BoxPathCollection])(BoxFolder.apply _)
  
  implicit val BoxFolderReads2: Reads[BoxFolder] = Json.reads[BoxFolder]
}