package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class BoxFile(id: String, sequence_id: String, name: String, pathCollection: Option[BoxPathCollection]) extends BoxSource {
  val fullPath = pathCollection.map{ path => s"${path.path}/$name"}
}

object BoxFile extends Function4[String, String, String, Option[BoxPathCollection], BoxFile]{
  implicit val BoxFileReads: Reads[BoxSource] = 
   ( (__ \ 'id).read[String] and
     (__ \ 'sequence_id).read[String] and
     (__ \ 'name).read[String] and
     (__ \ 'pathCollection).readNullable[BoxPathCollection])(BoxFile.apply _)
}