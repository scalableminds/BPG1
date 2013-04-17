package controllers

import play.api.mvc.Controller
import securesocial.core.SecureSocial
import models.UserDAO
import models.UserDAO._
import play.api.libs.json.Json
import projectZoom.util.PlayActorSystem
import models.User

object UserController extends ControllerBase with SecureSocial with CRUDController[User] {
  val dao = UserDAO
  
  val formatter = UserDAO.formatter
  
  def index = SecuredAction { implicit request =>
    Ok(views.html.index())
  }
}