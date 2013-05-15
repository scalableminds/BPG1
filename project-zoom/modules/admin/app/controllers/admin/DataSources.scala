package controllers.admin

import projectZoom.util.PlayConfig
import projectZoom.util.PlayActorSystem
import securesocial.core.SecureSocial
import play.api._
import play.api.mvc._
import controllers.common.ControllerBase

object DataSources extends ControllerBase with SecureSocial with PlayActorSystem with PlayConfig {
  
  def panel = SecuredAction { implicit request => 
    Ok(views.html.admin.dataSources())
  }
}