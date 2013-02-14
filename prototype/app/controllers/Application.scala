package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.{ DPhase, Workspace, WorkspaceController }

object Application extends WorkspaceController {

  val phaseForm = Form(
    "content" -> text)

  def index = Action {
    Ok(views.html.index(Workspace.findAll))
  }

  def showPhase(workspaceName: String, phase: String) = workspaceAction(workspaceName, phase) { request =>
    Ok(views.html.workspace(request.workspace, request.phase))
  }

  def updatePhase(workspaceName: String, phase: String) = workspaceAction(workspaceName, phase) { implicit request =>
    phaseForm.bindFromRequest.fold(
      failure => BadRequest("Bad text Data!"),
      newContent => {
        DPhase.addEntry(request.phase,newContent)
        Redirect(routes.Application.showPhase(workspaceName, request.phase.name))
      })
  }

  def createWorkspace(name: String) = Action {
    Workspace.create(name)
    Redirect(routes.Application.showPhase(name, DPhase.names(0)))
  }
}