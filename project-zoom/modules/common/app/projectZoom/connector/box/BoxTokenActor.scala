package projectZoom.connector.box

import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

case class BoxAccessTokens(access_token: String, access_token_expires: Long, token_type: String, refresh_token: String, refresh_token_expires: Long)

object BoxAccessTokens extends Function5[String, Long, String, String, Long, BoxAccessTokens]{
  
  val refresh_token_validity = 3600 * 24 * 14 //14 days
  
  implicit val boxAccessTokensFormat = Json.format[BoxAccessTokens]
  implicit val boxExpirationReader = 
    (__).json.update(
        ( __ \ 'access_token_expires).json.copyFrom((__ \ 'expires_in).json.pick[JsNumber].map{
          case JsNumber(t) => JsNumber(System.currentTimeMillis / 1000 + t)
        }
    )) andThen (__).json.update(
        (__ \ 'refresh_token_expires).json.put(
        JsNumber(System.currentTimeMillis() / 1000 + refresh_token_validity)
    )) andThen (__ \ 'expires_in).json.prune
      
}
case class UpdateBoxAccessTokens(tokens: BoxAccessTokens)

case object AccessTokensRequest

class BoxTokenActor(appKeys: BoxAppKeyPair, accessTokens: BoxAccessTokens) extends Actor{
  import BoxAccessTokens._
  
  private var tokens: BoxAccessTokens = accessTokens
  
  def isTokenValid : Boolean = System.currentTimeMillis() / 1000 + 200 < accessTokens.access_token_expires 
  
  def receive = {
    case AccessTokensRequest => sender ! refreshTokens
  }
  
  def refreshTokens: Option[BoxAccessTokens] = {
    if(isTokenValid) Some(tokens)
    else {
      val tokenRequest = WS.url("https://www.box.com/api/oauth2/token").post(
          Map("refresh_token" -> Seq(accessTokens.refresh_token),
              "client_id" -> Seq(appKeys.client_id),
              "client_secret" -> Seq(appKeys.client_secret),
              "grant_type" -> Seq("refresh_token"))
          )
      val response = Await.result(tokenRequest, 10 seconds)
      if(response.status == 200){
        Json.parse(response.body).validate(boxExpirationReader andThen boxAccessTokensFormat) match {
          case JsSuccess(newBoxTokens, _) => tokens = newBoxTokens
            Some(newBoxTokens)
          case JsError(errors) => Logger.error(errors.mkString)
            None
        }
      } else {
        Logger.error(response.body.toString)
        None
      }
    }
  }
}