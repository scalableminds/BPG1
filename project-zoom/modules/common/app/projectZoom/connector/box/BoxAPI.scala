package projectZoom.connector.box

import play.api.libs.json._
import play.api.libs.ws._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import scala.util.{Try, Success, Failure}

case class BoxAppKeyPair(client_id: String, client_secret: String)

class BoxAPI(appKeys: BoxAppKeyPair) {
  def getFolderInfo(accessToken: String, folder: String="0") = {
    val request = WS.url(s"https://api.box.com/2.0/folders/$folder")
      .withHeaders(("Authorization" -> s"Bearer $accessToken")).get
    
    val response = request.map(response => response.body)
    
    response.onComplete{
      case Success(response) => Logger.info(response)
      case Failure(error) => Logger.error(error.toString)
    }
  }
}