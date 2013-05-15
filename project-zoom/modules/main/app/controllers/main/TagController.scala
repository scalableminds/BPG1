package controllers.main
import models.TagDAO
import controllers.common.ControllerBase

object TagController extends ControllerBase with JsonCRUDController{
  val dao = TagDAO
  
}