package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

trait BoxMiniSource

object BoxMiniSource {
  implicit val BoxMiniSourceReads = (__).read(BoxMiniFile.BoxMiniFileAsSourceReads) or (__).read(BoxMiniFolder.BoxMiniFolderAsSourceReads)
}


trait BoxSource

object BoxSource {
  implicit val BoxSourceReads = (__).read(BoxFile.BoxFileAsSourceReads) or (__).read(BoxFolder.BoxFolderAsSourceReads)
}