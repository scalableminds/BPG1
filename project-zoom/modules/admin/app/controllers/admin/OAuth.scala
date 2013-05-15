package controllers.admin

import controllers.common.ControllerBase
import projectZoom.util.PlayConfig
import projectZoom.util.PlayActorSystem
import securesocial.core.SecureSocial
import play.api._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class BoxAccessTokens(access_token: String, expires_in: Int, token_type: String, refresh_token: String)

object BoxAccessTokens

object OAuth extends ControllerBase with SecureSocial with PlayActorSystem with PlayConfig {
  
  def beginBoxOAuth = SecuredAction { implicit request =>
    (for{client_id <- config.getString("box.client_id")
    } yield {
      Redirect(s"https://www.box.com/api/oauth2/authorize?response_type=code&client_id=$client_id&state=authenticated")
    }) getOrElse Ok("unfortunately box is not configured")
  }
  
  def boxAuthenticated(code: String, state: String) = SecuredAction { implicit request =>
    Async{
      (for{client_id <- config.getString("box.client_id")
           client_secret <- config.getString("box.client_secret")
      } yield {
        WS.url("https://www.box.com/api/oauth2/token").post(
          Map("grant_type" -> Seq("authorization_code"), 
              "code" -> Seq(code), 
              "client_id" -> Seq(client_id),
              "client_secret" -> Seq(client_secret))).map{ response =>
              Json.parse(response.body)
              Ok(response.body)
          }
      }) getOrElse Future.successful(Ok("retrieving box access token failed"))
    }
  }
}