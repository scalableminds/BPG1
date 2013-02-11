package models

import play.api._
import play.api.mvc._

case class WorkspaceRequest[T](val workspace: Workspace, val phase: DPhase, val request: Request[T]) extends WrappedRequest(request)

class WorkspaceController extends Controller {
  def workspaceAction[T](name: String, phaseName: String = "General", parser: BodyParser[T] = BodyParsers.parse.anyContent)(f: WorkspaceRequest[T] => Result) = {
    Action(parser) { implicit request: Request[T] =>
      
      (for {workspace <- Workspace.findByName(name)
           phaseId <- workspace.phases.get(phaseName)
           phase <- DPhase.findOneById(phaseId)} yield 
  
          f(WorkspaceRequest(workspace, phase, request))) getOrElse BadRequest("Workspace %s or %s does not exist".format(name, phaseName))
    }
  }
}

