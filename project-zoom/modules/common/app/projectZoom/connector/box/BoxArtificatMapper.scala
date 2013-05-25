package projectZoom.connector.box

import play.api.libs.json._
import models.ArtifactInfo
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.JsArray
import play.api.Logger

class BoxArtificatMapper(box: BoxAPI) {

  val infoFields = List("created_at", "created_by", "name")

  val pickEntries = (__ \ 'entries).json.pick[JsArray]

  def getArtifactsInfo(accessToken: String) = {
    box.folderContent(accessToken, "0", fields = infoFields)
  }

  def groupDirectories(jsArr: JsArray) = {
    jsArr.value.groupBy(entry => (entry \ "type").as[String])
  }

  def walkFolders(accessToken: String) = {

    def loop(currentFolders: List[JsValue], files: List[JsValue]) = {
      val newFoldersAndFiles =
        Future.traverse(currentFolders) { json =>
          box.folderContent(accessToken, (json \ "id").as[String], fields = infoFields)
        }
      newFoldersAndFiles.map(_.map{json => 
        json.transform(pickEntries) match {
          case JsSuccess(jsArr, _) => jsArr
          case JsError(err) => Logger.error(err.mkString) 
            JsArray
        }
      })
    }

    val rootFolders = box.folderContent(accessToken, "0", fields = infoFields)
  }
  
  
}