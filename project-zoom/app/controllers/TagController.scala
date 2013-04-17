package controllers

import play.api.mvc.Controller
import securesocial.core.SecureSocial
import models.Tag
import models.TagDAO
import play.api.libs.json.Json
import projectZoom.util.PlayActorSystem

object TagController extends ControllerBase with SecureSocial with PlayActorSystem {
  def index = SecuredAction { implicit request =>
    Ok(views.html.index())
  }

  def list(offset: Int, limit: Int) = SecuredAction { implicit request =>
    //TODO: restrict access
    Async {
      TagDAO.findSome(offset, limit).map { l =>
        Ok(Json.toJson(l))
      }
    }
  }
}