package controllers.main.project

import models.ProjectDAO
import play.api.libs.concurrent.Execution.Implicits._
import controllers.common.ControllerBase
import models.Implicits._
import models.GraphDAO
import models.GraphDAO._
import scala.concurrent.Future
import controllers.main.JsonCRUDController

object ProjectController extends ControllerBase with JsonCRUDController {
  val dao = ProjectDAO

  def createGraph(projectId: String) = SecuredAction(ajaxCall = true) { implicit request =>
    Async {
      GraphDAO.generateEmptyGraph(projectId).map { graph =>
        ProjectDAO.addGraphTo(projectId, graph).map { r =>
          if (r.updated > 0) {
            GraphDAO.insert(graph)
            Redirect(controllers.main.routes.GraphController.read(graph._id.stringify))
          } else {
            BadRequest("Project doesn't exist")
          }
        }
      }.getOrElse(Future.successful(BadRequest("Invalid Project id.")))
    }
  }

}