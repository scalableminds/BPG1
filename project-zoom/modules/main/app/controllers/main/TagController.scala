package controllers.main
import models.TagDAO

object TagController extends ControllerBase with JsonCRUDController{
  val dao = TagDAO
  
}