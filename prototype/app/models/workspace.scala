package models

import play.api.db._
import play.api.Play.current
import com.mongodb.casbah.Imports._
import models.context._
import com.novus.salat.annotations._
import com.novus.salat.dao.SalatDAO


case class Workspace(name: String, phases: Map[String, ObjectId], _id: ObjectId = new ObjectId) extends DAOCaseClass[Workspace]{
  val dao = Workspace
  val id = _id.toString
}

object Workspace extends BasicDAO[Workspace]("workspaces"){
  
  def findByName(name: String) = findOne(MongoDBObject("name" -> name));
  
  def create(name: String) = insertOne(Workspace(name, DPhase.names.map(phaseName => phaseName -> DPhase.create(phaseName)._id).toMap))
}