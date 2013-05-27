package controllers.main
import projectZoom.util.ExtendedTypes.ExtendedJsObject
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsError
import models.GraphDAO
import models.Graph
import projectZoom.core.event._
import play.api.libs.json.JsValue
import models.GraphTransformers
import models.Implicits._
import models.DBAccessContext
import play.api.libs.json.Json
import play.api.libs.json.JsObject
import controllers.common.ControllerBase
import play.api.libs.json.Reads
import play.api.Logger

case class GraphUpdated(graph: Graph, patch: JsValue) extends Event

object GraphController extends ControllerBase with JsonCRUDController with EventPublisher with GraphTransformers {
  val dao = GraphDAO

  override def singleObjectFinder(id: String)(implicit ctx: DBAccessContext) = {
    GraphDAO.findLatestForGroup(id)
  }

  override def displayReader(implicit ctx: DBAccessContext): Reads[JsObject] = {
    GraphDAO.includePayloadDetails(ctx)
  }

  def patch(groupId: String, baseVersion: Int) = SecuredAction(true, None, parse.json) { implicit request =>
    Async {
      val patch = request.body
      GraphDAO.findLatestForGroup(groupId).map {
        case Some(graph) if baseVersion == (graph \ "version").as[Int] =>
          (graph patchWith patch)
            .flatMap(graphFormat.reads)
            .map { updatedGraph =>
              Logger.warn("Updated:" + updatedGraph)
              GraphDAO.update(updatedGraph._id, updatedGraph).map { r =>
                Logger.warn("Insert result: " + r)
                publish(GraphUpdated(updatedGraph, patch))
              }
              JsonOk(GraphDAO.extractVersionInfo(updatedGraph), "graph.update.successful")
            }.recoverTotal {
              case e: JsError =>
                BadRequest(JsError.toFlatJson(e))
            }
        case Some(graph) =>
          Redirect(controllers.main.routes.GraphController.read(groupId))
        case _ =>
          NotFound
      }
    }
  }
}