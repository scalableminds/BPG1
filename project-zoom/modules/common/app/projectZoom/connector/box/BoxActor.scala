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
import scala.util.{ Try, Success, Failure }
import akka.pattern.ask

class BoxActor extends ArtifactAggregatorActor {

  Logger.debug(context.self.path.toString)
  val fileProjectMatcher = context.actorFor(s"${context.parent.path}/FileProjectMatcher")
  Logger.debug(fileProjectMatcher.path.toString)
  val tokenActor = context.actorOf(Props[BoxTokenActor])
  val box = new BoxExtendedAPI
  val BoxFileSystem = context.actorOf(BoxFileSystemTreeActor.props(fileProjectMatcher, "BoxFileSystem"))

  def getEventStreamPos = {
    Try {
      Await.result(DBProxy.getBoxEventStreamPos, 10 seconds)
    } match {
      case Success(eventStreamPosOpt) => eventStreamPosOpt.getOrElse(0.toLong)
      case Failure(err) =>
        Logger.error(s"Timeout reading BoxAccessTokens for BoxTokenActor from DB:\n $err")
        throw err
    }
  }

  def startUp(implicit accessTokens: BoxAccessTokens) = {
    box.enumerateEvents(getEventStreamPos).onComplete {
      case Success(t) => DBProxy.setBoxEventStreamPos(t._1)
      case Failure(err) => Logger.error(s"Error fetching current stream Position:\n$err")
    }
    traverseBoxFileStructure.map { x => BoxFileSystem ! PrintTree }
  }

  def traverseBoxFileStructure(implicit accessTokens: BoxAccessTokens) = {
    def loop(entries: List[BoxMiniSource]): List[BoxMiniSource] = {
      val x = entries.flatMap {
        case f @ BoxMiniFile(id, _, _, _) => box.fetchBoxFileInfo(id).map { file =>
          BoxFileSystem ! Add(file)
          List(f)
        }
        case BoxMiniFolder(id, _, _, _) => box.fetchBoxFolderInfo(id).map { folder =>
          BoxFileSystem ! Add(folder)
          box.fetchCollaboratorsEmail(folder.id).foreach { l =>
              BoxFileSystem ! AddCollaborations(folder, l.toSet)
            
          }
          folder.item_collection match {
            case Some(itemCollection) => loop(itemCollection.entries)
            case None => List()
          }
        }
      }
      x.flatten
    }
    box.fetchBoxRootInfo.flatMap { folder => folder.item_collection } match {
        case Some(items) => loop(items.entries)
        case None =>
          Logger.error("No Items")
          List()
    }
  }

  def findProjectForFile(file: BoxFile, collaborators: Set[String]) = {
    Logger.debug("About to ask FileProjectMatcher for project")
    (fileProjectMatcher ? BoxFileInfo(file.name, file.pathString, collaborators)).mapTo[Option[ProjectLike]]
  }

  def downloadAndPublishFile(file: BoxFile, collaborators: Set[String])(implicit accessTokens: BoxAccessTokens) = {
    box.downloadFile(file.id).map { byteArray =>
      Logger.debug(s"finished downloading file with id ${file.id}")
      findProjectForFile(file, collaborators).foreach { projectOpt =>
        projectOpt.foreach { project =>
          Logger.debug(s"found ${file.fullPath} to be in project ${project.name}")
          publishFoundArtifact(byteArray, Artifact(file.name, project.name, file.pathString, "box", file.created_at.getMillis(), Json.toJson(file)))
        }
      }
    }
  }

  def handleEventStream(implicit accessTokens: BoxAccessTokens) {

    val jsonResponse = box.fetchEvents(getEventStreamPos)
    val nextEventStreamPos = (jsonResponse \ "next_stream_position").as[Long]
    DBProxy.setBoxEventStreamPos(nextEventStreamPos)
    Logger.debug(s"new eventStreamPos: $nextEventStreamPos")
    val eventList = (jsonResponse \ "entries").as[JsArray].value.map { json =>
      json.validate(api.BoxEvent.BoxEventReads) match {
        case JsSuccess(event, _) =>
          if (event.event_type == "ITEM_RENAME")
            Some(event)
        case JsError(err) =>
          Logger.error(s"Error validating BoxEvents:\n${err.mkString}")
          Logger.debug(s"json:\n${Json.stringify(json)}")
          None
      }
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
    withAccessToken { implicit token =>
      //handleEventStream    
    }
  }

  def boxActorStarted: Receive = {
    case NewFile(f: BoxFile, collaborators) =>
      withAccessToken { implicit token =>
        downloadAndPublishFile(f, collaborators)
      }
  }

  def boxActorStopped: Receive = {
    case BoxUpdated(tokens) =>
      DBProxy.setBoxToken(tokens)
      DBProxy.unsetBoxEventStreamPos
  }

  override def started: Receive = super.started.orElse(boxActorStarted)

  override def stopped: Receive = super.stopped.orElse(boxActorStopped)

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 minute) {
    case NoBoxConfigException(msg) => Escalate
    case _ => Restart
  }

  def start() = {
    Logger.debug("Starting BoxActor")
    tokenActor ! InitializeBoxTokenActor
    withAccessToken { implicit token =>
      box.fetchBoxRootInfo.foreach{ folder =>
        BoxFileSystem ! InitializeTree(folder)
        startUp
      }
    }
  }
  def stop() = {
    Logger.debug("Stopping BoxActor")
    tokenActor ! ResetBoxTokenActor
    BoxFileSystem ! ResetTree
  }
}