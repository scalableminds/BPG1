package projectZoom.connector.box

import api._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.Logger

import akka.agent._
import projectZoom.util.PlayActorSystem
import akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import projectZoom.util.PlayConfig

class BoxExtendedAPI extends BoxAPI with PlayActorSystem {

  implicit val timeout = Timeout(5 seconds)
  val collaborations = Agent[Map[String, List[BoxMiniUser]]](Map())

  def getRelevantStreamPosition(implicit accessTokens: BoxAccessTokens): Long = {
    def loop(streamPosition: Long): Long = {
      val json = fetchEvents(streamPosition)
      val chunkSize = (json \ "chunk_size").as[Int]
      val nextStreamPosition = (json \ "next_stream_position").as[Long]
      if (chunkSize == 0)
        nextStreamPosition
      else
        loop(nextStreamPosition)
    }
    loop(0)
  }

  def enumerateEvents(streamPos: Long = 0)(implicit accessTokens: BoxAccessTokens) = {
    def loop(stream_position: Long, accumulatedEvents: JsArray): Future[(Long, Future[JsArray])] = {
      Logger.debug(s"fetching events at stream pos $stream_position")
      val json = fetchEvents(stream_position)
      val chunkSize = (json \ "chunk_size").as[Int]
      val nextStreamPos = (json \ "next_stream_position").as[Long]
      Logger.debug(s"fetched $chunkSize events")
      if (chunkSize == 0)
        Future((nextStreamPos, Future(accumulatedEvents)))
      else
        loop(nextStreamPos, accumulatedEvents ++ (json \ "entries").as[JsArray])
    }
    loop(streamPos, JsArray())
  }

  def fetchCollaborators(file: BoxFile)(implicit accessTokens: BoxAccessTokens): Option[List[BoxMiniUser]] =
    collaborations().get(file.parent.id)

  def fetchCollaborators(folder: BoxBaseFolder)(implicit accessTokens: BoxAccessTokens): Option[List[BoxMiniUser]] =
    collaborations().get(folder.id)

  def buildCollaboratorCache(events: List[BoxEvent])(implicit accessTokens: BoxAccessTokens) = {
    val folderIds = events.map { event =>
      event.source match {
        case Some(file: BoxFile) => file.parent.id
        case Some(folder: BoxFolder) => folder.id
      }
    }.distinct

    folderIds.foreach{fId => 
      val collaboratorsListOpts = fetchCollaboratorsList(fId)
      folderIds.zip(collaboratorsListOpts).foreach { p =>
          collaborations.send(_ + (p._1 -> p._2))
      }
    }
  }

  def fetchCollaboratorsEmail(folderId: String)(implicit accessTokens: BoxAccessTokens): Option[List[String]] = {
    fetchCollaboratorsList(folderId).map(_.map(_.login))
  }

  def fetchCollaboratorsList(folderId: String)(implicit accessTokens: BoxAccessTokens): Option[List[BoxMiniUser]] = {
    val json = fetchFolderCollaborations(folderId)
    (json \ "entries").validate[List[BoxCollaboration]] match {
      case JsSuccess(collaborationList, _) =>
        val collaborators = collaborationList.flatMap(c => c.accessible_by)
        Some(collaborators)
      case JsError(err) =>
        Logger.error(s"Error fetching collaborations:\n ${err.mkString}\njson:\n${Json.stringify(json)}")
        None
    }

  }

  def fetchBoxFileInfo(fileId: String)(implicit accessTokens: BoxAccessTokens) = {
    import BoxFile.BoxFileReads
    val fileJson = fetchFileInfo(fileId)
    fileJson.validate[BoxFile] match {
      case JsSuccess(f, _) => Some(f)
      case JsError(err) =>
        Logger.error(s"Error fetching file($fileId) Info:\n ${err.mkString}\njson:\n${Json.stringify(fileJson)}")
        None
    }
  }

  def fetchBoxRootInfo(implicit accessTokens: BoxAccessTokens) = fetchBoxFolderInfo("0")

  def fetchBoxFolderInfo(folderId: String)(implicit accessTokens: BoxAccessTokens): Option[BoxFolder] = {
    import BoxFolder.BoxFolderReads
    val folderJson = fetchFolderInfo(folderId)
    folderJson.validate[BoxFolder] match {
      case JsSuccess(f, _) => Some(f)
      case JsError(err) =>
        Logger.error(s"Error fetching folder($folderId) Info:\n ${err.mkString}\njson:\n${Json.stringify(folderJson)}")
        None
    }

  }

}

object BoxExtendedAPI extends PlayConfig {
  def create = {

  }
}