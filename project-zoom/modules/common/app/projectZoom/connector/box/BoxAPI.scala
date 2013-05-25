package projectZoom.connector.box

import play.api.libs.json._
import play.api.libs.ws._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import scala.util.{Try, Success, Failure}

case class BoxAppKeyPair(client_id: String, client_secret: String)

class BoxAPI(appKeys: BoxAppKeyPair) {
  
  def auth(accessToken: String) = ("Authorization" -> s"Bearer $accessToken")
  
  def getFolderInfo(accessToken: String, folder: String="0"): Future[JsValue] = {
    val request = WS.url(s"https://api.box.com/2.0/folders/$folder")
      .withHeaders(auth(accessToken)).get
    
    request.map(response => Json.parse(response.body))
  }
  
  def downloadFile(accessToken: String, fileId: String) = {
    val request = WS.url(s"https://api.box.com/2.0/files/$fileId/content")
      .withHeaders(auth(accessToken)).get
   
   // ITERATEEEEE OH MY GOD
  }
  
}