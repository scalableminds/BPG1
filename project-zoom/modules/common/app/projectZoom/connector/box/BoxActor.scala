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
import models.Artifact

class BoxActor(appKeys: BoxAppKeyPair, accessTokens: BoxAccessTokens, var eventStreamPos: Long) extends ArtifactAggregatorActor {
  type FolderId=String
  
  val TICKER_INTERVAL = 1 minute
  
  implicit val timeout = Timeout(30 seconds)
  
  lazy val tokenActor = context.actorOf(Props(new BoxTokenActor(appKeys, accessTokens)))
  lazy val box = new BoxExtendedAPI(appKeys)
 
  val collaborations = scala.collection.mutable.Map[FolderId, List[String]]()

  var updateTicker: Cancellable = null
  
  def findProjectForFile(file: BoxFile)(implicit accessTokens: BoxAccessTokens): Future[String] = {
    box.fetchCollaborators(file).map{ collaborators =>
      val emailAddresses = collaborators.map(_.login)
      projects.maxBy{ project =>
        project.participants.count(p => emailAddresses.contains(p._user))
      }.name
    }
  }
  
  def handleITEM_UPLOAD(event: BoxEvent)(implicit accessTokens: BoxAccessTokens) {
    event.source.map{ source =>
      source match {
        case file: BoxFile => 
          //box.downloadFile(file.id).map{byteArray =>
          findProjectForFile(file).foreach{ projectName =>
            Logger.info(s"found ${file.fullPath} to be in project $projectName")
            //publishFoundArtifact(byteArray, Artifact(file.name, projectName, "box", file.path, Json.parse("{}")))
          }
        //}
      }
    }
  }
  
  def handleEventStream(implicit accessTokens: BoxAccessTokens) {
    box.enumerateEvents(eventStreamPos).map { p =>
      DBProxy.setBoxEventStreamPos(p._1)
      eventStreamPos = p._1
      p._2.map{eventArr =>
        eventArr.value.map { json =>
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
        case eventList => eventList.foreach{ event =>
          event.event_type match {
            case "ITEM_UPLOAD" => handleITEM_UPLOAD(event)
            case otherType => Logger.debug(s"event of Type '$otherType' found")
          }
        }
      }
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