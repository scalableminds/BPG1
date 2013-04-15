package controllers

import play.api.mvc.Controller
import securesocial.core.SecureSocial

object ProjectController extends ControllerBase with SecureSocial{
  def index = SecuredAction { implicit request =>
    Ok(views.html.index())
  
  }  
}