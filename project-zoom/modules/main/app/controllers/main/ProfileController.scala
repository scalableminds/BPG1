package controllers.main
import securesocial.core.SecureSocial
import models.ProfileDAO
import controllers.common.ControllerBase
import play.api.libs.json.Reads
import models.GraphDAO
import models.DBAccessContext
import play.api.libs.json.JsObject

object ProfileController extends ControllerBase with SecureSocial with JsonCRUDController {
  val dao = ProfileDAO

  override def displayReader(implicit ctx: DBAccessContext): Reads[JsObject] = {
    ProfileDAO.removeSensitiveInformation
  }

}