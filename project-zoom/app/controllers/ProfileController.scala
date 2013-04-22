package controllers

import play.api.mvc.Controller
import securesocial.core.SecureSocial
import play.api.libs.json.Json
import projectZoom.util.PlayActorSystem
import models.User
import models.ProfileDAO

object ProfileController extends ControllerBase with SecureSocial with JsonCRUDController {
  val dao = ProfileDAO
  
  def index = SecuredAction { implicit request =>
    Ok(views.html.index())
  }
}