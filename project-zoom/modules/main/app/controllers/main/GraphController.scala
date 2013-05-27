package controllers.main

import projectZoom.util.ExtendedTypes.ExtendedJsObject
import play.api.libs.concurrent.Execution.Implicits._
import models.GraphDAO
import models.Graph
import projectZoom.core.event._
import models.GraphTransformers
import models.Implicits._
import models.DBAccessContext
import play.api.libs.json._
import play.api.libs.json.Reads._
import controllers.common.ControllerBase
import play.api.Logger
import play.api.libs.functional.syntax._

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
            .flatMap((GraphDAO.incrementVersion and GraphDAO.generateId).reduce.reads)
            .map { v => Logger.error(v.toString); v }
            .flatMap(graphFormat.reads)
            .map { updatedGraph =>
              Logger.trace("Updated graph: " + updatedGraph)
              GraphDAO.insert(updatedGraph).map { r =>
                publish(GraphUpdated(updatedGraph, patch))
              }
              JsonOk(GraphDAO.extractVersionInfo(updatedGraph), "graph.patch.successful")
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