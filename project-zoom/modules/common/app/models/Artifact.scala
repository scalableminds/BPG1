package models

import projectZoom.util.DBCollection
import play.api.libs.json._
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import reactivemongo.core.commands.LastError
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

/**
 * An Artifact describes a documentation content object.
 */
trait ArtifactLike {
  /**
   * Name of the artifact
   */
  def name: String
  /**
   * Name of the project the artifact relates to
   */
  def projectName: String
  /**
   * Path of the artifact in the original source. The source should be able
   * to identify the artifact using the path
   */
  def path: String
  /**
   * Origin of the artifact.
   */
  def source: String
  /**
   * Time of creation
   */
  def createdAt: Long
  /**
   * Additional data the data source provides
   */
  def metadata: JsValue
  /**
   * Specifies if the artifact was deleted from the data source
   */
  def isDeleted: Boolean
}

/**
 * Converters of the ArtifactInterface
 */
trait ArtifactLikeTransformers extends ResourceLikeTransformers {

  /**
   * Creates a tuple from an artifact like object
   */
  def toTuple(a: ArtifactLike) =
    (a.name, a.projectName, a.path, a.source, a.createdAt, a.metadata, a.isDeleted)

  /**
   * Converts smth artifact like to json
   */
  implicit val artifactLikeWrites =
    ((__ \ 'name).write[String] and
      (__ \ 'projectName).write[String] and
      (__ \ 'path).write[String] and
      (__ \ 'source).write[String] and
      (__ \ 'createdAt).write[Long] and
      (__ \ 'metadata).write[JsValue] and
      (__ \ 'isDeleted).write[Boolean])(toTuple _)
}

/**
 * An Artifact
 */
case class Artifact(
  name: String,
  projectName: String,
  path: String,
  source: String,
  createdAt: Long,
  metadata: JsValue,
  isDeleted: Boolean = false,
  resources: List[Resource] = Nil,
  _id: BSONObjectID = BSONObjectID.generate)
    extends ArtifactLike

/**
 * Converters from and to an artifact
 */
trait ArtifactTransformers extends ResourceHelpers {
  implicit val artifactFormat: Format[Artifact] = Json.format[Artifact]
}

/**
 * Factory for artifacts
 */
trait ArtifactFactory extends ArtifactTransformers {
  def createArtifactFrom(js: JsObject) = {
    js.asOpt[Artifact]
  }
}

/**
 * DAO for artifacts
 */
object ArtifactDAO
    extends SecuredMongoJsonDAO[Artifact]
    with ArtifactFactory
    with ResourceHelpers
    with ArtifactLikeTransformers
    with ArtifactTransformers {

  /**
   * Name of the DB collection
   */
  val collectionName = "artifacts"

  def findByArtifactQ(artifact: ArtifactLike): JsObject =
    findByArtifactQ(artifact.name, artifact.source, artifact.projectName)

  def findByArtifactQ(name: String, source: String, projectName: String) =
    Json.obj(
      "name" -> name,
      "source" -> source,
      "projectName" -> projectName)

  def findByResourceQ(resource: ResourceLike): JsObject =
    Json.obj(
      "resources.typ" -> resource.typ,
      "resources.name" -> resource.name)

  def findOne(artifact: ArtifactLike)(implicit ctx: DBAccessContext) =
    collectionFind(findByArtifactQ(artifact)).one[JsObject]

  def update(artifact: ArtifactLike)(implicit ctx: DBAccessContext): Future[LastError] =
    collectionUpdate(findByArtifactQ(artifact),
      Json.obj("$set" -> artifact, "$setOnInsert" -> Json.obj("resources" -> Json.arr())), upsert = true)

  def markAsDeleted(artifact: ArtifactLike)(implicit ctx: DBAccessContext) =
    collectionUpdate(findByArtifactQ(artifact),
      Json.obj("$set" -> Json.obj(
        "isDeleted" -> true)))

  def findSomeForProject(_project: String, offset: Int, limit: Int)(implicit ctx: DBAccessContext) =
    takeSome(findForProject(_project), offset, limit)

  def findAllForProject(_project: String)(implicit ctx: DBAccessContext) =
    findForProject(_project).cursor[JsObject].toList

  def findForProject(projectName: String)(implicit ctx: DBAccessContext) =
    collectionFind(Json.obj(
      "projectName" -> projectName))

  def findAllForSource(_source: String)(implicit ctx: DBAccessContext) =
    findForSource(_source).cursor[JsObject].toList

  def findForSource(source: String)(implicit ctx: DBAccessContext) =
    collectionFind(Json.obj(
      "source" -> source))

  def findResource(artifact: ArtifactLike, resource: ResourceLike)(implicit ctx: DBAccessContext) = {
    collectionFind(findByArtifactQ(artifact) ++ findByResourceQ(resource)).one[Artifact].map(
      _.flatMap(_.resources.find(_.isSameAs(resource))))
  }

  def insertResource(artifact: ArtifactLike)(hash: String, resource: ResourceLike)(implicit ctx: DBAccessContext) = {
    val hashedResource = resource.withHash(hash)
    collectionUpdate(findByArtifactQ(artifact), Json.obj(
      "$addToSet" -> Json.obj(
        "resources" -> hashedResource))).flatMap { _ =>
      collectionFind(findByArtifactQ(artifact)).one[Artifact]
    }
  }

  def updateHashOfResource(artifact: ArtifactLike)(hash: String, resource: ResourceLike)(implicit ctx: DBAccessContext) = {
    collectionUpdate(findByArtifactQ(artifact) ++ findByResourceQ(resource.withHash(hash)), Json.obj(
      "$set" -> Json.obj(
        "resources.$.hash" -> hash))).flatMap { _ =>
      collectionFind(findByArtifactQ(artifact)).one[Artifact]
    }
  }
}