package controllers

import play.api.mvc.Controller
import securesocial.core.SecureSocial
import models.Project
import models.ProjectDAO
import play.api.libs.json.Json
import projectZoom.util.PlayActorSystem

object ProjectController extends ControllerBase with SecureSocial with PlayActorSystem{
  def index = SecuredAction { implicit request =>
    Ok(views.html.index())
  
  }  
  
  def list(offset: Int, limit: Int) = SecuredAction{ implicit request =>
    //TODO: restrict access
    Async{
      ProjectDAO.findSome(offset, limit).map { l =>
        Ok(Json.toJson(l))
      }
    }
  }
  
  def read(id: String) = SecuredAction{ implicit request =>
    //TODO: restrict access
    Async{
      ProjectDAO.findById(id).map { l =>
        Ok(Json.toJson(l))
      }
    }
  }
}