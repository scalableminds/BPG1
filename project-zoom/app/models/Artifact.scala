package models

import projectZoom.util.DBCollection
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.Logger
import projectZoom.core.bson.Bson._
import play.api.libs.json.JsString

case class ArtifactInfo(name: String, _project: String, source: String, metadata: JsValue)

trait ArtifactInfoFactory{
  implicit val artifactInfoFormat = Json.format[ArtifactInfo]
  
  def createFrom(js: JsObject) = {
    js.asOpt[ArtifactInfo]
  }
}

object Artifact extends MongoJson with ArtifactInfoFactory{
  override def collection = db("artifacts")
  
  
  def findByArtifactQ(artifactInfo: ArtifactInfo): JsObject = 
    findByArtifactQ(artifactInfo.name, artifactInfo.source, artifactInfo._project)
    
  def findByArtifactQ(name: String, source: String, project: String) = 
    Json.obj("name" -> name, "source" -> source, "_project" -> project)
  
  def find(artifactInfo: ArtifactInfo) = {
    collection.find(findByArtifactQ(artifactInfo)).one
  }
  
  def update(artifactInfo: ArtifactInfo) = {
    collection.update(findByArtifactQ(artifactInfo), 
        Json.obj("$set" -> artifactInfo), upsert = true)
  }
  
  def markAsDeleted(artifactInfo: ArtifactInfo) = {
    collection.update(findByArtifactQ(artifactInfo), 
        Json.obj("$set" -> Json.obj("isDeleted" -> true)))
  }
  
  def findAllForProject(_project: String) = {
    collection.find(Json.obj("_project" -> _project)).cursor.toList
  }
  
  def insertRessource(artifactInfo: ArtifactInfo)(path: String, hash: String, resourceInfo: ResourceInfo) = {
    val resource = Resource.createFrom(resourceInfo, path, hash)
    Resource.update(resource).map{ lastError =>
      if(!lastError.updatedExisting){
        lastError.get("_id") match {
          case Some(id: JsString) =>
            collection.update(findByArtifactQ(artifactInfo), Json.obj("$addToSet" -> Json.obj( "resources" -> id)))
          case _ =>
            Logger.error("insertResource: Couldn't extract id field from last error")
        }
      }
      lastError
    }
  }
}