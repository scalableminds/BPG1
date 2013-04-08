package models

import projectZoom.util.DBCollection
import play.api.libs.json.JsObject

case class Artifact(name: String, source: String, createdAt: Long, updatedAt: Long, userName: Option[String])

object Artifact extends MongoJson {
  override def collection = db("artifacts")
  
  def updateArtifact() = {
    
  }
}