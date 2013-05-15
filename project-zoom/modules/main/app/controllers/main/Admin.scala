package controllers.main

import projectZoom.util.PlayConfig
import projectZoom.util.PlayActorSystem
import securesocial.core.SecureSocial
import play.api._
import play.api.mvc._

object Admin extends ControllerBase with SecureSocial with PlayActorSystem with PlayConfig {
  
  def panel = SecuredAction { implicit request => 
    Ok(views.html.main.adminPanel())
  }
}