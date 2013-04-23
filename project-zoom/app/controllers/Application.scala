package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Redirect(controllers.routes.Application.processView)
  }

  def processView = Action {
    Ok(views.html.processView())
  }

  def overview = Action {
    Ok(views.html.projectsOverview())
  }

  def test = Action {
    Ok(views.html.test())
  }

}