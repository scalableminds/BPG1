package controllers.admin

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.admin.index("Your new application is ready."))
  }
  
}