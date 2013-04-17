package controllers

import play.api.mvc.Controller
import securesocial.core.SecureSocial
import models.UserDAO
import models.UserDAO._
import play.api.libs.json.Json
import projectZoom.util.PlayActorSystem

object UserController extends ControllerBase with SecureSocial with PlayActorSystem {
  def index = SecuredAction { implicit request =>
    Ok(views.html.index())
  }

  def list(offset: Int, limit: Int) = SecuredAction { implicit request =>
    //TODO: restrict access
    Async {
      UserDAO.findSome(offset, limit).map { l =>
        Ok(Json.toJson(l))
      }
    }
  }
}