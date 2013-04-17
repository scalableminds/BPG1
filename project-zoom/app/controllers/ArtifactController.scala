package controllers

import securesocial.core.SecureSocial
import models.ArtifactDAO
import play.api.libs.json.Json
import projectZoom.util.PlayActorSystem

object ArtifactController extends ControllerBase with SecureSocial with PlayActorSystem{
  def index = SecuredAction { implicit request =>
    Ok(views.html.index())
  }  
  
  def list(offset: Int, limit: Int) = SecuredAction{ implicit request =>
    //TODO: restrict access
    Async{
      ArtifactDAO.findSome(offset, limit).map { l =>
        Ok(Json.toJson(l))
      }
    }
  }
}