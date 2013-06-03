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


class BoxExtendedAPI(appKeys: BoxAppKeyPair) extends BoxAPI(appKeys) with PlayActorSystem {
  type FolderId=String
  
  implicit val timeout = Timeout(5 seconds)
  val collaborations = Agent[Map[FolderId, List[BoxMiniUser]]](Map())
  
  def enumerateEvents(streamPos: Long = 0)(implicit accessTokens: BoxAccessTokens) = {
    def loop(stream_position: Long, accumulatedEvents: JsArray): Future[(Long, Future[JsArray])] = {
      Logger.debug(s"fetching events at stream pos $stream_position")
      fetchEvents(stream_position).flatMap { json =>
        val chunkSize = (json \ "chunk_size").as[Int]
        val nextStreamPos = (json \ "next_stream_position").as[Long]
        Logger.debug(s"fetched $chunkSize events")
        if (chunkSize == 0)
          Future((nextStreamPos, Future(accumulatedEvents)))
        else
          loop(nextStreamPos, accumulatedEvents ++ (json \ "entries").as[JsArray])
      }
    }
    loop(streamPos, JsArray())
  }
  
  def fetchCollaborators(file: BoxFile)(implicit accessTokens: BoxAccessTokens): Future[List[BoxMiniUser]] = 
    fetchCollaboratorsList(file.parent)
  
  def fetchCollaborators(folder: BoxBaseFolder)(implicit accessTokens: BoxAccessTokens): Future[List[BoxMiniUser]] = 
    fetchCollaboratorsList(folder)
    
  private def fetchCollaboratorsList(folder: BoxBaseFolder)(implicit accessTokens: BoxAccessTokens): Future[List[BoxMiniUser]] = {
    collaborations.future.map{map => map(folder.id)} fallbackTo
    fetchFolderCollaborations(folder.id).map{ json =>
      (json \ "entries").validate[List[BoxCollaboration]] match {
        case JsSuccess(collaborationList, _) => 
          val collaborators = collaborationList.flatMap(c => c.accessible_by)
          collaborations.send(_ + (folder.id -> collaborators))
          collaborators
        case JsError(err) => Logger.error(s"Error fetching collaborations:\n ${err.mkString}\njson:\n${Json.stringify(json)}")
        List()
      }
    }
  }
}