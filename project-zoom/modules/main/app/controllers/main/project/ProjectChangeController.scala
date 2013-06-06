package controllers.main.project

import controllers.common.ControllerBase
import securesocial.core.SecureSocial
import play.api.mvc.WebSocket
import play.api.libs.json.JsValue
import scala.concurrent.Future

object ProjectChangeController extends ControllerBase with SecureSocial with ClosedChannelHelper {

  def joinChannel(_project: String) = WebSocket.async[JsValue] { implicit request =>
    SecureSocial.currentUser(request).map { user =>
      ProjectRoom.join(user.id, _project)
    } getOrElse {
      Future.successful(closedChannel("Not logged in."))
    }
  }
}