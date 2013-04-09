package controllers

import play.api.mvc.Action
import projectZoom.util.ExtendedTypes.ExtendedJsObject
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsError
import models.Graph
import play.api.libs.json.JsSuccess
import projectZoom.core.event._
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

case class GraphUpdated(graph: JsObject, patch: JsValue) extends Event

object GraphController extends ControllerBase with EventPublisher{

  def patch(graphId: String) = Action(parse.json) { implicit request =>
    Async {
      val patch = request.body
      Graph.findById(graphId).map {
        case Some(graph) =>
          (graph patchWith patch)
            .flatMap(_.transform(Graph.removePayloadDetails))
            .map { updatedGraph =>
              Graph.insert(updatedGraph).map{ _ =>
                publish(GraphUpdated(updatedGraph, patch))
              }
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