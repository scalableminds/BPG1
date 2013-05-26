package projectZoom.connector.box

import play.api.libs.json._
import models.ArtifactInfo
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.JsArray
import play.api.Logger
import projectZoom.connector.box.api._

class BoxArtifactMapper(box: BoxAPI) {

  val infoFields = List("created_at", "created_by", "name")

  def getArtifacts(implicit accessTokens: BoxAccessTokens) = {
    val eventList = box.enumerateEvents.map { eventArr =>
      eventArr.value.flatMap { json =>
        json.validate(api.BoxEvent.BoxEventReads) match {
          case JsSuccess(event, _) => Some(event)
          case JsError(err) =>
            Logger.error(err.mkString)
            None
        }
      }
    }

    val files = eventList.map {
      _.filter(event => event.event_type == "ITEM_UPLOADED")
        .flatMap { event =>
          event.source match {
            case Some(file @ BoxFile(_, _, _, _)) => Some(file)
            case _ => None
          }
        }
    }

    files.map { fileSeq =>
      fileSeq.map { file =>
        (box.downloadFile(file.id), ArtifactInfo(file.name, "", "box", Json.parse("{}")))
      }
    }
  }
}