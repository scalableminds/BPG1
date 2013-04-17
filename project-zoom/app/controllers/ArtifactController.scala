package controllers

import securesocial.core.SecureSocial
import models.ArtifactDAO
import play.api.libs.json.Json
import projectZoom.util.PlayActorSystem

object ArtifactController extends ControllerBase with JsonCRUDController{

  val dao = ArtifactDAO
  
  def index = SecuredAction { implicit request =>
    Ok(views.html.index())
  }  
}