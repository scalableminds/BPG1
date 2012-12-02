package models

import play.api.db._
import play.api.Play.current
import com.mongodb.casbah.Imports._
import models.context._
import com.novus.salat.annotations._
import com.novus.salat.dao.SalatDAO


case class Workspace(name: String, phases: List[DPhase], _id: ObjectId = new ObjectId) {
  val dao = Workspace
  val id = _id.toString
  
  def addPhase(name: String) = {
    this.copy(phases = phases :+ DPhase(name, "template text"))
  }
  
  def updatePhase(phaseId: Int, content: String) = {
    this.copy(phases = phases.updated(phaseId, DPhase(phases(phaseId).name, content)))
  }
}

object Workspace extends BasicDAO[Workspace]("workspaces"){
  
  def findByName(name: String) = findOne(MongoDBObject("name" -> name));
  
  def create(name: String) = {
    val workspace = Workspace(name, List[DPhase](DPhase("Understand", "template text")))
    insert ( workspace )
    workspace
  }
  
  def addPhase(workspace: Workspace, name: String) = {
    val updatedWorkspace = workspace.addPhase(name)
    save (updatedWorkspace)
    updatedWorkspace
  }
  
  def updatePhase(workspace: Workspace, phaseId: Int, content: String) = {
    if(workspace.phases.size <= phaseId || phaseId < 0) throw new IllegalArgumentException
    val updatedPhase = workspace.updatePhase(phaseId, content)
    save (updatedPhase)
    updatedPhase
  }
}