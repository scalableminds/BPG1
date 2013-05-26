package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

trait BoxSource

object BoxSource {
  implicit val BoxSourceReads = (__).read(BoxFile.BoxFileReads) or (__).read(BoxFolder.BoxFolderReads)
}