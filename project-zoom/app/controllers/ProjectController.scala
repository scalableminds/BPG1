package controllers

import play.api.mvc.Controller
import securesocial.core.SecureSocial
import models.Project
import models.ProjectDAO
import play.api.libs.json.Json
import projectZoom.util.PlayActorSystem

object ProjectController extends ControllerBase with JsonCRUDController{
  val dao = ProjectDAO
  
}