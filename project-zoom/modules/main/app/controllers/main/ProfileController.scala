package controllers.main
import securesocial.core.SecureSocial
import models.ProfileDAO
import controllers.common.ControllerBase

object ProfileController extends ControllerBase with SecureSocial with JsonCRUDController {
  val dao = ProfileDAO
  
}