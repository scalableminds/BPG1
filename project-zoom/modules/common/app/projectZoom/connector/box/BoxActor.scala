package projectZoom.connector.box

import projectZoom.connector._
import akka.actor._
import akka.actor.SupervisorStrategy._
import akka.agent._
import scala.concurrent.duration._
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import akka.pattern._
import akka.util.Timeout
import play.api.libs.json._
import api._
import models.{ Artifact, ProjectLike }
import org.joda.time.DateTime
import scala.util.{Try, Success, Failure }
import akka.pattern.ask

class BoxActor extends ArtifactAggregatorActor {
  
  val fileProjectMatcher = context.actorFor(s"${context.parent.path}/FileProjectMatcher")
  val tokenActor = context.actorOf(Props[BoxTokenActor])
  val box = new BoxExtendedAPI
  val BoxFileSystem = context.actorOf(BoxFileSystemTreeActor.props(fileProjectMatcher, "BoxFileSystem"))
  
  def getEventStreamPos = {
    Try {
      Await.result(DBProxy.getBoxEventStreamPos, 10 seconds)    
    } match {
      case Success(eventStreamPosOpt) => eventStreamPosOpt.getOrElse(0.toLong)
      case Failure(err) => Logger.error(s"Timeout reading BoxAccessTokens for BoxTokenActor from DB:\n $err")
      throw err
    }
  }
  
  def boxActorStopped: Receive = {
    case BoxUpdated(tokens) => 
      DBProxy.setBoxToken(tokens)
      DBProxy.unsetBoxEventStreamPos
  }
  
  def startUp(implicit accessTokens: BoxAccessTokens) = {
    box.enumerateEvents(getEventStreamPos).onComplete{
      case Success(t) => DBProxy.setBoxEventStreamPos(t._1)
      case Failure(err) => Logger.error(s"Error fetching current stream Position:\n$err")
    }
    traverseBoxFileStructure.map{x => BoxFileSystem ! PrintTree}
   
  }
  
  def traverseBoxFileStructure(implicit accessTokens: BoxAccessTokens) = {
    def loop(entries: List[BoxMiniSource]): Future[List[BoxMiniSource]] = {
      val x = (Future.traverse(entries){ 
        case f@BoxMiniFile(id,_,_, _) => box.fetchBoxFileInfo(id).flatMap{ fileOpt => 
            fileOpt.foreach(file => BoxFileSystem ! Add(file))
            Future(List(f))
        }.recover{ 
          case err => Logger.error(s"Error fetching file info:$err")
              List[BoxMiniSource]()
        }
        case BoxMiniFolder(id,_,_, _) => box.fetchBoxFolderInfo(id).flatMap{ folderOpt => 
            folderOpt.foreach(folder => BoxFileSystem ! Add(folder))
            folderOpt.foreach(folder => box.fetchCollaboratorsEmail(folder.id).foreach{_.foreach{l => 
              BoxFileSystem ! AddCollaborations(folder, l.toSet)}})
            val itemCollectionOpt = folderOpt.flatMap(_.item_collection)
            itemCollectionOpt match {
              case Some(itemCollection) => loop(itemCollection.entries)
              case None => Future(List())
            }

        }.recover {
          case err => Logger.error(s"Error fetching file info:$err")
          List[BoxMiniSource]()
        } 
      })
      x.map(_.flatten)
    }
    box.fetchBoxRootInfo.flatMap{folderOpt => 
      folderOpt.flatMap{folder => folder.item_collection} match {
        case Some(items) => loop(items.entries)
        case None => Logger.error("No Items")
        Future(List())
      }
    }
  }
  
  override def stopped: Receive = super.stopped.orElse(boxActorStopped)

  def findProjectForFile(file: BoxFile)(implicit accessTokens: BoxAccessTokens): Option[ProjectLike] = {
    val collaboratorsOpt = box.fetchCollaborators(file)
    collaboratorsOpt.flatMap { collaborators =>
      val collaboratorEMails = collaborators.map(_.login)
      Await.result((fileProjectMatcher ? BoxFileInfo(file.name, file.pathString, collaboratorEMails)).mapTo[Option[ProjectLike]], 30 seconds)
    }
  }

  def handleITEM_UPLOAD(events: List[BoxEvent])(implicit accessTokens: BoxAccessTokens) {
    box.buildCollaboratorCache(events).onComplete {
      case x =>
        events.foreach { event =>
          event.source match {
            case Some(file: BoxFile) =>
              box.downloadFile(file.id).map { byteArray =>
                findProjectForFile(file).foreach { project =>
                  val sourceJson = (event.json \ "source")
                  Logger.debug(s"found ${file.fullPath} to be in project ${project.name}")
                  publishFoundArtifact(byteArray, Artifact(file.name, project.name, file.pathString, "box", file.created_at.getMillis(), sourceJson))
                }
              }
            case Some(folder: BoxFolder) =>
              Logger.debug(s"found folder being uploaded: $folder")
          }
        }
    }
  }

  def handleITEM_RENAME(events: List[BoxEvent])(implicit accessTokens: BoxAccessTokens) {

  }

  def handleEventStream(implicit accessTokens: BoxAccessTokens) {
    
    box.fetchEvents(getEventStreamPos).map { jsonResponse =>
      val nextEventStreamPos = (jsonResponse \ "next_stream_position").as[Long]
      DBProxy.setBoxEventStreamPos(nextEventStreamPos)
      Logger.debug(s"new eventStreamPos: $nextEventStreamPos")
      (jsonResponse \ "entries").as[JsArray].value.map { json =>
        json.validate(api.BoxEvent.BoxEventReads) match {
          case JsSuccess(event, _) =>
            if (event.event_type == "ITEM_RENAME")
              Logger.debug(s"json:\n${Json.stringify(json)}")
            Some(event)
          case JsError(err) =>
            Logger.error(s"Error validating BoxEvents:\n${err.mkString}")
            Logger.debug(s"json:\n${Json.stringify(json)}")
            None
        }
      }.flatten
    }
      .onSuccess {
        case eventList =>
          Logger.debug(s"got ${eventList.size} events")
          val eventMap = eventList.groupBy { event => event.event_type }
          handleITEM_UPLOAD(eventMap.get("ITEM_UPLOAD").map(_.toList) getOrElse Nil)
          handleITEM_RENAME(eventMap.get("ITEM_RENAME").map(_.toList) getOrElse Nil)
      }
  }
  
  def withAccessToken(f: BoxAccessTokens => Unit) = {
    (tokenActor ? AccessTokensRequest).mapTo[Option[BoxAccessTokens]].map { tokenOpt =>
      tokenOpt.map { token =>
        f(token)
      }
    }
  }

  def aggregate() = {
    withAccessToken{ implicit token =>
        //handleEventStream    
    }
  }
  
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 minute) {
    case NoBoxConfigException(msg) => Escalate
    case _ => Restart
  }
  
  def start() = {
    Logger.debug("Starting BoxActor")
    tokenActor ! InitializeBoxTokenActor
    withAccessToken{implicit token => 
      box.fetchBoxRootInfo.onComplete{
        case Success(folderOpt) => 
          Logger.debug(folderOpt.toString)
          folderOpt.foreach{folder => 
            Logger.debug(folder.toString)
            BoxFileSystem ! InitializeTree(folder)
            startUp}
        case Failure(err) => Logger.error(s"Error getting BoxRootInfo for startup:\n$err")
      }
    
    }
  }
  def stop() = {
    Logger.debug("Stopping BoxActor")
    tokenActor ! ResetBoxTokenActor
    BoxFileSystem ! ResetTree
  }
}