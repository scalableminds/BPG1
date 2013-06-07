package projectZoom.connector.box

import projectZoom.connector._
import akka.actor._
import scala.concurrent.duration._
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import akka.pattern._
import akka.util.Timeout
import play.api.libs.json._
import api._
import models.{Artifact, ProjectLike}
import org.joda.time.DateTime

import scala.util.{Success,Failure}

class BoxActor(appKeys: BoxAppKeyPair, accessTokens: BoxAccessTokens, var eventStreamPos: Long) extends ArtifactAggregatorActor {

  Logger.debug(projects.mkString("\n"))
  val TICKER_INTERVAL = 1 minute
  
  implicit val timeout = Timeout(30 seconds)
  
  lazy val tokenActor = context.actorOf(Props(new BoxTokenActor(appKeys, accessTokens)))
  lazy val box = new BoxExtendedAPI(appKeys)

  var updateTicker: Cancellable = null
  
  def findProjectForFile(file: BoxFile)(implicit accessTokens: BoxAccessTokens): Option[ProjectLike] = {
    val collaboratorsOpt = box.fetchCollaborators(file)
    collaboratorsOpt.flatMap{collaborators =>
      val collaboratorEMails = collaborators.map(_.login.toLowerCase).toSet
      FileProjectMatcher(file.path, collaboratorEMails, new DateTime(file.created_at))
    }
  }
  
  def handleITEM_UPLOAD(events: List[BoxEvent])(implicit accessTokens: BoxAccessTokens) {
    box.buildCollaboratorCache(events).onComplete{
      case x => 
      events.foreach{ event =>
        event.source match {
          case Some(file: BoxFile) => 
            //box.downloadFile(file.id).map{ byteArray =>
                findProjectForFile(file).foreach{project => 
                  Logger.debug(s"found ${file.fullPath} to be in project ${project.name}")
                  //publishFoundArtifact(byteArray, Artifact(file.name, projectName, file.path, "box", Json.parse("{}")))
                }
            //}
          case Some(folder: BoxFolder) =>
            Logger.debug(s"found folder being uploaded: $folder")
        }
      }
    }
  }
  
  def handleEventStream(implicit accessTokens: BoxAccessTokens) {
    box.fetchEvents(eventStreamPos).map { jsonResponse => 
      eventStreamPos = (jsonResponse \ "next_stream_position").as[Long]
      DBProxy.setBoxEventStreamPos(eventStreamPos)
      Logger.debug(s"new eventStreamPos: $eventStreamPos")
      (jsonResponse \ "entries").as[JsArray].value.map { json =>
          json.validate(api.BoxEvent.BoxEventReads) match {
            case JsSuccess(event, _) => Some(event)
            case JsError(err) =>
              Logger.error(s"Error validating BoxEvents:\n${err.mkString}")
              Logger.debug(s"json:\n${Json.stringify(json)}")
              None
          }
        }.flatten
      }
      .onSuccess{
        case eventList => 
          val eventMap = eventList.groupBy{ event => event.event_type}
          handleITEM_UPLOAD(eventMap.get("ITEM_UPLOAD").map(_.toList) getOrElse Nil)
      }
    }  
  
  def aggregate() = {
    (tokenActor ? AccessTokensRequest).mapTo[Option[BoxAccessTokens]].map{tokenOpt => 
      tokenOpt.map{ implicit token => 
        context.parent ! UpdateBoxAccessTokens(token)
        handleEventStream
      }
    }
  }
  
  def start = {
    Logger.debug("Starting update ticker")
    updateTicker = context.system.scheduler.schedule(0 seconds, TICKER_INTERVAL, self, Aggregate)
  }

  def stop = {
    updateTicker.cancel
  }
  
  override def preStart(){
    self ! StartAggregating
  }
}