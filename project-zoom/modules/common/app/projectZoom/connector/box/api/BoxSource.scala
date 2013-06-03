package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

trait BoxMiniSource

object BoxMiniSource {
  implicit val BoxMiniSourceReads = BoxMiniFile.BoxMiniFileAsSourceReads or BoxMiniFolder.BoxMiniFolderAsSourceReads
}


trait BoxSource

object BoxSource {
  implicit val BoxSourceReads = 
    BoxFile.BoxFileAsSourceReads or 
    BoxFolder.BoxFolderAsSourceReads or 
    BoxCollaboration.boxCollaborationAsSourceReads or
    BoxComment.BoxCommentAsSourceReads or
    BoxLock.BoxLockAsSourceReads
}