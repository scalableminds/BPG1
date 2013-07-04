package controllers.main

import controllers.common.ControllerBase
import play.api._
import play.api.mvc._
import securesocial.core.SecureSocial

object Application extends ControllerBase with SecureSocial {

  def index = SecuredAction { implicit request =>
    Ok(views.html.main.index())
  }

  def test = SecuredAction { implicit request =>
    Ok(views.html.main.test())
  }

}