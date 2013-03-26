package controllers

import play.api.mvc.Action
import projectZoom.util.ExtendedTypes.ExtendedJsObject
import scala.concurrent.Future
//import models.Graph
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsError

object GraphController extends ControllerBase {

  def patch(graphId: String) = Action(parse.json) { implicit request =>
    Async{
       /*Graph.findById(graphId).map{
         case Some(graph) =>
           (graph patchWith request.body).map(_.transform(removePayloadDetails andThen ).map{
             
             Ok
           }.recoverTotal{ e =>
             BadRequest
           }
         case _ =>
           NotFound
       }*/
      ???
    }
  }
}