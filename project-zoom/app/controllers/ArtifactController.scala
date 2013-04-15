package controllers

import securesocial.core.SecureSocial

object ArtifactController extends ControllerBase with SecureSocial{
  def index = SecuredAction { implicit request =>
    Ok(views.html.index())
  
  }  
}