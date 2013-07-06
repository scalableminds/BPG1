package projectZoom.connector.box

import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import projectZoom.connector.DBProxy
import scala.util.{Try, Success, Failure}

case object AccessTokensRequest
case object InitializeBoxTokenActor
case object ResetBoxTokenActor

case class NoBoxConfigException(msg: String) extends Exception
case class NoBoxAccessTokensException(msg: String) extends Exception

class BoxTokenActor extends Actor{
  import BoxAccessTokens._
  import context.become
  
  private def getTokens = {
    val accessTokenFut = DBProxy.getBoxTokens
    Try{
      Await.result(accessTokenFut, 10 seconds)    
    } match {
      case Success(Some(accessTokens)) => accessTokens
      case Success(None) => throw new NoBoxAccessTokensException("No BoxAccessTokens for BoxTokenActor found in DB")
      case Failure(err) => Logger.error(s"Timeout reading BoxAccessTokens for BoxTokenActor from DB:\n $err")
      throw err
    }
  }
  
  def isTokenValid(tokens: BoxAccessTokens) : Boolean = (System.currentTimeMillis() / 1000 + 200) < tokens.access_token_expires 
  
  def uninitialized: Receive = {
    case InitializeBoxTokenActor =>     
      BoxAppKeyPair.readFromConfig match {
      case Some(appKeyPair) => become(initialized(appKeyPair))
      case _ => throw new NoBoxConfigException("No config for Box found")
    }
    case AccessTokensRequest => sender ! None
  }
  
  def initialized(appKeys: BoxAppKeyPair): Receive = {
    case AccessTokensRequest => sender ! refreshTokens(appKeys)
    case ResetBoxTokenActor => become(uninitialized)
  }
  
  def receive = uninitialized
  
  def refreshTokens(appKeys: BoxAppKeyPair): Option[BoxAccessTokens] = { 
    val tokens = getTokens
    if(isTokenValid(tokens)) Some(tokens)
    else {
      Logger.debug("fetching new box access token")
      val tokenRequest = WS.url("https://www.box.com/api/oauth2/token").post(
          Map("refresh_token" -> Seq(tokens.refresh_token),
              "client_id" -> Seq(appKeys.client_id),
              "client_secret" -> Seq(appKeys.client_secret),
              "grant_type" -> Seq("refresh_token"))
          )
      val response = Await.result(tokenRequest, 10 seconds)
      if(response.status == 200){
        Json.parse(response.body).validate(boxExpirationReader andThen boxAccessTokensFormat) match {
          case JsSuccess(newBoxTokens, _) =>
            DBProxy.setBoxToken(newBoxTokens)
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