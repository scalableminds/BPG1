package controllers.main
import models.ProjectDAO
import play.api.libs.concurrent.Execution.Implicits._
import controllers.common.ControllerBase
import models.Implicits._
import models.GraphDAO
import models.GraphDAO._

object ProjectController extends ControllerBase with JsonCRUDController {
  val dao = ProjectDAO

  def createGraph(projectId: String) = SecuredAction(ajaxCall = true) { implicit request =>
    Async {
      val graph = GraphDAO.generateEmptyGraph
      ProjectDAO.addGraphTo(projectId, graph).map { r =>
        if (r.updated > 0) {
          GraphDAO.insert(graph)
          Redirect(controllers.main.routes.GraphController.read(graph._id.stringify))
        } else {
          BadRequest("Project doesn't exist")
        }
      }
    }
  }

}