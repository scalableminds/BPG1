package controllers

import projectZoom.util.PlayConfig
import projectZoom.util.PlayActorSystem
import securesocial.core.SecureSocial
import play.api._
import play.api.mvc._

object OAuth extends ControllerBase with SecureSocial with PlayActorSystem with PlayConfig {
  
  def beginBoxOAuth = SecuredAction { implicit request =>
    (for{client_id <- config.getString("box.client_id")
    } yield {
      Redirect(s"https://www.box.com/api/oauth2/authorize?response_type=code&client_id=$client_id&state=authenticated")
    }) getOrElse Ok("unfortunately box is not configured")
  }
  
  def boxAuthenticated(code: String, state: String) = SecuredAction { implicit request =>
    Logger.info(s"code: $code")
    Logger.info(s"state: $state")
    Ok(views.html.adminPanel())
  }
}