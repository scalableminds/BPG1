package controllers

import play.api._
import play.api.mvc._
import securesocial.core.SecureSocial

object Application extends ControllerBase with SecureSocial {

  def index = Action {
    Redirect(controllers.routes.Application.processView)
  }

  def processView = SecuredAction { implicit request =>
    Ok(views.html.processView())
  }

  def overview = SecuredAction { implicit request =>
    Ok(views.html.projectsOverview())
  }

  def test = Action {
    Ok(views.html.test())
  }

}