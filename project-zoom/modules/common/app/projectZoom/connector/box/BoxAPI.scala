package projectZoom.connector.box

import play.api.libs.json._
import play.api.libs.ws._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import scala.util.{Try, Success, Failure}

case class BoxAppKeyPair(client_id: String, client_secret: String)

class BoxAPI(appKeys: BoxAppKeyPair) {
  
  val apiURL = "https//api.box/com/2.0"
  
  def authHeader(accessToken: String) = ("Authorization" -> s"Bearer $accessToken")
  
  private def accessAPI(accessToken: String, url: String): Future[JsValue] = {
    WS.url(url)
      .withHeaders(authHeader(accessToken))
      .get
      .map(response => Json.parse(response.body))
  }
  
  def folderInfo(accessToken: String, folderId: String): Future[JsValue] = {
    accessAPI(accessToken, s"$apiURL/folders/$folderId")
  }
  
  def folderContent(accessToken: String, folderId: String, offset: Int = 0, fields: List[String] = Nil, limit: Int = 1000): Future[JsValue] = {
    accessAPI(accessToken,s"$apiURL/folders/$folderId/items?offset=$offset&fields=${fields.mkString(",")}&limit=$limit")
  }
  
  def completeFolderContent(accessToken: String, folderId: String, fields: List[String]): Future[JsValue] = ???
  
  def fileInfo(accessToken: String, fileId: String) = {
    accessAPI(accessToken, (s"$apiURL/files/$fileId"))
  }
  
  def events(accessToken: String, stream_position: Long = 0) = {
    accessAPI(accessToken, (s"$apiURL/events?stream_position=$stream_position"))
  }
  
  def downloadFile(accessToken: String, fileId: String) = {
    val request = WS.url(s"$apiURL/files/$fileId/content")
      .withHeaders(authHeader(accessToken)).get
   
   // ITERATEEEEE OH MY GOD
  }
  
}