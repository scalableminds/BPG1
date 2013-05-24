package controllers.main

import controllers.common.ControllerBase
import play.api._
import play.api.mvc._
import securesocial.core.SecureSocial

object Application extends ControllerBase with SecureSocial {

  def index = Action {
    Redirect(controllers.main.routes.Application.processView)
  }

  def processView = SecuredAction { implicit request =>
    Ok(views.html.main.processView())
  }

  def overview = SecuredAction { implicit request =>
    Ok(views.html.main.projectsOverview())
  }

  def test = SecuredAction { implicit request =>
    Ok(views.html.main.test())
  }

}