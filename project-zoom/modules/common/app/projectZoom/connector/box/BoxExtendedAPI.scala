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
  
  def fetchCollaborators(file: BoxFile)(implicit accessTokens: BoxAccessTokens): List[BoxMiniUser] = 
    collaborations().get(file.parent.id).getOrElse(Nil)
  
  def fetchCollaborators(folder: BoxBaseFolder)(implicit accessTokens: BoxAccessTokens): List[BoxMiniUser] = 
    collaborations().get(folder.id).getOrElse(Nil)
    
  def buildCollaboratorCache(events: List[BoxEvent])(implicit accessTokens: BoxAccessTokens) = {
    val folderIds = events.map{event => 
      event.source match {
        case Some(file: BoxFile) => file.parent.id
        case Some(folder: BoxFolder) => folder.id
      }
    }.distinct
    
    Future.traverse(folderIds)(fId => fetchCollaboratorsList(fId)).map{ collaboratorsListList =>
      folderIds.zip(collaboratorsListList).foreach{p => collaborations.send(_ + (p._1 -> p._2))}
    }    
  }
    
  private def fetchCollaboratorsList(folderId: FolderId)(implicit accessTokens: BoxAccessTokens): Future[List[BoxMiniUser]] = {
    fetchFolderCollaborations(folderId).map{ json =>
      (json \ "entries").validate[List[BoxCollaboration]] match {
        case JsSuccess(collaborationList, _) => 
          val collaborators = collaborationList.flatMap(c => c.accessible_by) 
          collaborators
        case JsError(err) => Logger.error(s"Error fetching collaborations:\n ${err.mkString}\njson:\n${Json.stringify(json)}")
        List()
      }
    }
  }
}