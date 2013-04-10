package models

import projectZoom.util.DBCollection
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.Logger
import projectZoom.core.bson.Bson._
import play.api.libs.json.JsString
import play.api.libs.json.Format

case class ArtifactInfo(name: String, _project: String, source: String, metadata: JsValue)

case class Artifact(id: String, name: String, source: String, _project: String, metadata: JsValue, resources: Map[String, Resource])

trait ArtifactInfoFactory {
  implicit val artifactInfoFormat = Json.format[ArtifactInfo]

  def createArtifactFrom(js: JsObject) = {
    js.asOpt[ArtifactInfo]
  }
}

trait ArtifactTransformers extends ResourceHelpers {
  implicit val artifactFormat: Format[Artifact] = Json.format[Artifact]
  //val outputArtifact = ???

}

object ArtifactDAO extends MongoJsonDAO with ArtifactInfoFactory with ResourceHelpers {
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
    val resource = resourceCreateFrom(resourceInfo, path, hash)

    collection.update(findByArtifactQ(artifactInfo), Json.obj(
      "$set" -> Json.obj(
        s"resources.${resourceInfo.typ}" -> resource)))
  }
}