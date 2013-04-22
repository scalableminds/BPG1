package models

import projectZoom.util.DBCollection
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.Logger
import play.api.libs.json.JsString
import play.api.libs.json.Format
import play.api.libs.concurrent.Execution.Implicits._

/* 
 * ArtifactInfo needs to be a subset of artifact. It should contain all 
 * necessary information to create a new artifact.
 */
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
  val collectionName = "artifacts"

  def findByArtifactQ(artifactInfo: ArtifactInfo): JsObject =
    findByArtifactQ(artifactInfo.name, artifactInfo.source, artifactInfo._project)

  def findByArtifactQ(name: String, source: String, project: String) =
    Json.obj("name" -> name, "source" -> source, "_project" -> project)

  def findOne(artifactInfo: ArtifactInfo) =
    collection.find(findByArtifactQ(artifactInfo)).one[JsObject]

  def update(artifactInfo: ArtifactInfo) =
    collection.update(findByArtifactQ(artifactInfo),
      Json.obj("$set" -> artifactInfo), upsert = true)

  def markAsDeleted(artifactInfo: ArtifactInfo) =
    collection.update(findByArtifactQ(artifactInfo),
      Json.obj("$set" -> Json.obj("isDeleted" -> true)))

  def findSomeForProject(_project: String, offset: Int, limit: Int) =
    takeSome(findForProject(_project), offset, limit)

  def findAllForProject(_project: String) =
    findForProject(_project).cursor[JsObject].toList

  def findForProject(_project: String) =
    collection.find(Json.obj("_project" -> _project))

  def insertRessource(artifactInfo: ArtifactInfo)(path: String, hash: String, resourceInfo: ResourceInfo) = {
    val resource = resourceCreateFrom(resourceInfo, path, hash)

    collection.update(findByArtifactQ(artifactInfo), Json.obj(
      "$set" -> Json.obj(
        s"resources.${resourceInfo.typ}" -> resource)))
  }
}