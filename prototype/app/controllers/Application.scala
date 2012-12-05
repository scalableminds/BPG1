package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.Workspace

object Application extends Controller {
  
  def BadWorkspace(name: String) = BadRequest("workspace %s does not exist".format(name)) 
  
  val phaseForm = Form(
      "content" -> text
  )
  
  def index = Action {
    Ok(views.html.index(Workspace.findAll))
  }
  
  def showWorkspace(name: String) = Action {
    val workspaceOpt = Workspace.findByName(name)
    if (workspaceOpt.isDefined) {
      val workspace = workspaceOpt.get
      Redirect(routes.Application.showPhase(name, 0))
    }
    else
      BadRequest("workspace does not exist")
  }
  
  def showPhase(workspaceName: String, phase: Int) = Action {
    val workspaceOpt = Workspace.findByName(workspaceName)
    if(workspaceOpt.isDefined){
      val workspace = workspaceOpt.get
      if (phase < workspace.phases.size && phase >= 0)
        Ok(views.html.workspace(workspace, phase))
      else
        BadRequest("phase %d does not exist".format(phase))
    }
    else
      BadWorkspace(workspaceName)
  }
  
  def updatePhase(workspaceName: String, phase: Int) = Action { implicit request => 
    val newContent = phaseForm.bindFromRequest.get
    val workspaceOpt = Workspace.findByName(workspaceName)
    if(workspaceOpt.isDefined){
      val workspace = workspaceOpt.get
      if(phase >= 0 && phase < workspace.phases.size){
        Workspace.updatePhase(workspace, phase, newContent)
        Redirect(routes.Application.showPhase(workspaceName, phase))
      }
      else BadRequest("phase %d does not exist".format(phase))   
    }
    else BadWorkspace(workspaceName) 
  }

  /*
  def addPhase(workspaceName: String) = Action {
    val workspaceOpt = Workspace.findByName(workspaceName)
    val phaseType = "Understand"
    if(workspaceOpt.isDefined){
      val workspace = workspaceOpt.get
      Workspace.addPhase(workspace, phaseType)
      Redirect(routes.Application.showPhase(workspaceName, workspace.phases.size - 1))
    }
    else BadWorkspace(workspaceName)
  }
  // */
  
  def createWorkspace(name: String) = Action {
    Workspace.create(name)
    Redirect(routes.Application.showWorkspace(name))
  }
}