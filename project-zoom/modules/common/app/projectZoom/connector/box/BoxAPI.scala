package projectZoom.connector.box

import play.api.libs.json._
import play.api.libs.ws._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import scala.util.{ Try, Success, Failure }
import play.api.libs.iteratee.Iteratee

case class BoxAppKeyPair(client_id: String, client_secret: String)

class BoxAPI(appKeys: BoxAppKeyPair) {

  val pickEntries = (__ \ 'entries).json.pick[JsArray]

  val apiURL = "https://api.box.com/2.0"

  def authHeader(implicit accessTokens: BoxAccessTokens) = ("Authorization" -> s"Bearer ${accessTokens.access_token}")

  private def jsonAPI(url: String)(implicit accessTokens: BoxAccessTokens): Future[JsValue] = {
    Logger.debug(s"calling jsonAPI on url $url")
    WS.url(url)
      .withHeaders(authHeader)
      .get
      .map(response => Json.parse(response.body))
  }

  def fetchFolderInfo(folderId: String)(implicit accessTokens: BoxAccessTokens): Future[JsValue] = {
    jsonAPI(s"$apiURL/folders/$folderId")
  }

  def fetchFolderContent(folderId: String,
    offset: Int = 0,
    fields: List[String] = Nil,
    limit: Int = 1000)(implicit accessTokens: BoxAccessTokens): Future[JsValue] = {
    jsonAPI(s"$apiURL/folders/$folderId/items?offset=$offset&fields=${fields.mkString(",")}&limit=$limit")
  }

  def fetchFileInfo(fileId: String)(implicit accessTokens: BoxAccessTokens) = {
    jsonAPI(s"$apiURL/files/$fileId")
  }

  def fetchEvents(stream_position: Long = 0)(implicit accessTokens: BoxAccessTokens) = {
    jsonAPI(s"$apiURL/events?stream_position=$stream_position")
  }

  def downloadFile(fileId: String)(implicit accessTokens: BoxAccessTokens) = {
    Logger.debug(s"downloading file with Id $fileId")
    WS.url(s"$apiURL/files/$fileId/content")
      .withHeaders(authHeader)
      .get(status => Iteratee.consume[Array[Byte]]())
      .flatMap(i => i.run)
  }

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
}