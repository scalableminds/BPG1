package controllers

import play.api.mvc.Action
import projectZoom.util.ExtendedTypes.ExtendedJsObject
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsError
import models.Graph
import play.api.libs.json.JsSuccess

object GraphController extends ControllerBase {

  def patch(graphId: String) = Action(parse.json) { implicit request =>
    Async {
      val patch = request.body
      Graph.findById(graphId).map {
        case Some(graph) =>
          (graph patchWith patch)
            .flatMap(_.transform(Graph.removePayloadDetails))
            .map { updatedGraph =>
              Graph.insert(updatedGraph)
              Ok
            }
            .recoverTotal {
              case e: JsError =>
                BadRequest(e.toString)
            }
        case _ =>
          NotFound
      }
    }
  }
}