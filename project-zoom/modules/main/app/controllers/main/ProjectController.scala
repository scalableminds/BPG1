package controllers.main
import models.ProjectDAO
import controllers.common.ControllerBase

object ProjectController extends ControllerBase with JsonCRUDController{
  val dao = ProjectDAO
  
}