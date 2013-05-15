package controllers.main
import models.ProjectDAO

object ProjectController extends ControllerBase with JsonCRUDController{
  val dao = ProjectDAO
  
}