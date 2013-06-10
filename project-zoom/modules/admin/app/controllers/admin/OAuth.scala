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
import models.PermanentValueService
import projectZoom.connector.box.BoxAccessTokens
import projectZoom.connector.BoxUpdated
import projectZoom.core.event._

object OAuth extends ControllerBase with SecureSocial with PlayActorSystem with PlayConfig with EventPublisher {

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
              Json.parse(response.body).transform(BoxAccessTokens.boxExpirationReader) match {
                case JsSuccess(boxAccessTokens, _) => 
                  PermanentValueService.put("box.tokens", boxAccessTokens)
                  boxAccessTokens.validate[BoxAccessTokens] match {
                    case JsSuccess(accessTokens, _) => publish(BoxUpdated(accessTokens))
                    case JsError(errors) => Logger.error(errors.mkString)
                  }
                  Ok(views.html.admin.dataSources())
                case JsError(errors) => Logger.error(errors.mkString)
                  Ok(errors.mkString)
              }
          }
      }) getOrElse Future.successful(Ok("retrieving box access token failed"))
    }
  }
}