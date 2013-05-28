package models

import projectZoom.util.DBCollection
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.Logger
import play.api.libs.json.JsString
import play.api.libs.json.Format
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import reactivemongo.core.commands.LastError
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

/* 
 * ArtifactInfo needs to be a subset of artifact. It should contain all 
 * necessary information to create a new artifact.
 */
trait ArtifactLike {
  def name: String
  def path: String
  def projectName: String
  def source: String
  def metadata: JsValue
}
case class ArtifactInfo(name: String, projectName: String, path: String, source: String, metadata: JsValue)
  extends ArtifactLike

case class Artifact(name: String, projectName: String, path: String, source: String, metadata: JsValue, resources: List[Resource], _id: BSONObjectID)
  extends ArtifactLike

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

object ArtifactDAO extends SecuredMongoJsonDAO[Artifact] with ArtifactInfoFactory with ResourceHelpers with ArtifactTransformers {
  val collectionName = "artifacts"

  def findByArtifactQ(artifactInfo: ArtifactInfo): JsObject =
    findByArtifactQ(artifactInfo.name, artifactInfo.source, artifactInfo.projectName)

  def findByArtifactQ(name: String, source: String, projectName: String) =
    Json.obj("name" -> name, "source" -> source, "projectName" -> projectName)
    
  def findByResourceQ(resourceInfo: ResourceInfo): JsObject =
    Json.obj("resources.typ" -> resourceInfo.typ, "resources.name" -> resourceInfo.name)
    
  def findOne(artifactInfo: ArtifactInfo)(implicit ctx: DBAccessContext) =
    collectionFind(findByArtifactQ(artifactInfo)).one[JsObject]

  def update(artifactInfo: ArtifactInfo)(implicit ctx: DBAccessContext): Future[LastError] =
    collectionUpdate(findByArtifactQ(artifactInfo),
      Json.obj("$set" -> artifactInfo), upsert = true)

  def markAsDeleted(artifactInfo: ArtifactInfo)(implicit ctx: DBAccessContext) =
    collectionUpdate(findByArtifactQ(artifactInfo),
      Json.obj("$set" -> Json.obj("isDeleted" -> true)))

  def findSomeForProject(_project: String, offset: Int, limit: Int)(implicit ctx: DBAccessContext) =
    takeSome(findForProject(_project), offset, limit)

  def findAllForProject(_project: String)(implicit ctx: DBAccessContext) =
    findForProject(_project).cursor[JsObject].toList

  def findForProject(projectName: String)(implicit ctx: DBAccessContext) =
    collectionFind(Json.obj("projectName" -> projectName))

  def findResource(artifactInfo: ArtifactInfo, resourceInfo: ResourceInfo)(implicit ctx: DBAccessContext) =
    collectionFind(findByArtifactQ(artifactInfo) ++ findByResourceQ(resourceInfo)).one[Artifact].map(
      _.flatMap(_.resources.find( _.isSameAs(resourceInfo))))
    
  def insertResource(artifactInfo: ArtifactInfo)(hash: String, resourceInfo: ResourceInfo)(implicit ctx: DBAccessContext) = {
    val resource = resourceCreateFrom(resourceInfo, hash)

    collectionUpdate(findByArtifactQ(artifactInfo), Json.obj(
      "$addToSet" -> Json.obj(
        s"resources" -> resource)))
  }
  
  def updateHashOfResource(artifactInfo: ArtifactInfo)(hash: String, resourceInfo: ResourceInfo)(implicit ctx: DBAccessContext) = {
    val resource = resourceCreateFrom(resourceInfo, hash)

    collectionUpdate(findByArtifactQ(artifactInfo) ++ findByResourceQ(resourceInfo), Json.obj(
      "$set" -> Json.obj(
        "resources.$.hash" -> hash)))
  }
}