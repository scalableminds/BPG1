package projectZoom.core.artifact

import akka.actor.Actor
import java.io.File
import play.api.libs.json.JsValue
import projectZoom.util.DBCollection
import java.io.InputStream
import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api.Logger
import projectZoom.core.event._
import projectZoom.util.StartableActor
import models.ArtifactDAO
import models.Artifact
import models.Artifact
import models.Resource
import play.api.Play
import java.io.FileOutputStream
import java.io.FileInputStream
import models.Resource
import java.security.MessageDigest
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import play.api.libs.concurrent.Execution.Implicits._
import models.DefaultResourceTypes
import models.GlobalDBAccess
import models.ResourceLike
import scala.concurrent.Future
import reactivemongo.core.commands.LastError
import models.ArtifactLike

trait ArtifactUpdate extends Event

case class RequestResource( artifactLike: ArtifactLike, resource: ResourceLike)
case class UpdateInfo(origin: String, projectName: String)

/*
 * Subscribed to
 */
case class ArtifactFound(originalStream: InputStream, artifact: ArtifactLike) extends ArtifactUpdate
case class ArtifactDeleted(artifact: ArtifactLike) extends ArtifactUpdate
case class ArtifactAggregation(_project: String, l: List[ArtifactFound]) extends ArtifactUpdate
case class ArtifactRenamed(artifact: ArtifactLike, name: String) extends ArtifactUpdate
case class ArtifactMoved(artifact: ArtifactLike, path: String) extends ArtifactUpdate
case class ResourceFound(inputStream: InputStream, artifact: ArtifactLike, resource: ResourceLike) extends Event

/*
 * Publishes
 */
case class ArtifactUpdated(artifact: ArtifactLike) extends Event
case class ArtifactInserted(artifact: ArtifactLike) extends Event

case class ResourceUpdated(file: File, artifact: ArtifactLike, resource: ResourceLike) extends Event
case class ResourceInserted(file: File, artifact: ArtifactLike, resource: ResourceLike) extends Event

trait FSWriter {
  val basePath = {
    val p = Play.current.configuration.getString("core.resource.basePath") getOrElse "data"
    val f = new File(p)
    f.mkdirs()
    f.getAbsolutePath()
  }

  def pathFor(projectName: String, artifactPath: String, resource: ResourceLike) =
    s"$basePath/$projectName/${artifactPath}/${resource.typ}/${resource.name}"

  def fileFor(projectName: String, artifactPath: String, resource: ResourceLike) = {
    val path = pathFor(projectName, artifactPath, resource)
    val file = new File(path)
    if (file.getAbsolutePath().startsWith(basePath))
      Some(file)
    else
      None
  }

  def writeToFS(is: InputStream, projectName: String, artifactPath: String, resource: ResourceLike) = {
    fileFor(projectName, artifactPath, resource).map {
      case file =>
        file.getParentFile().mkdirs
        val os = new FileOutputStream(file)
        val writtenBytes = org.apache.commons.io.IOUtils.copyLarge(is, os)
        is.available()
        os.close()
        file
    }
  }

  def readFromFS(projectName: String, artifactPath: String, resource: ResourceLike): Option[InputStream] = {
    fileFor(projectName, artifactPath, resource).map {
      case file =>
        new FileInputStream(file)
    }
  }
}

class ArtifactActor extends EventSubscriber with EventPublisher with FSWriter with GlobalDBAccess {

  def handleResourceUpdate(is: InputStream, artifact: ArtifactLike, resource: ResourceLike) = {
    writeToFS(is, artifact.projectName, artifact.path, resource).map {
      case file =>
        val hash = DigestUtils.md5Hex(FileUtils.readFileToByteArray(file))
        ArtifactDAO.findResource(artifact, resource).map {
          case None =>
            ArtifactDAO.insertResource(artifact)(hash, resource).map( updated =>
              publish(ResourceInserted(file, updated getOrElse artifact, resource)))
          case Some(r) if r.hash.map(_ != hash) getOrElse true =>
            ArtifactDAO.updateHashOfResource(artifact)(hash, resource).map(updated =>
              publish(ResourceUpdated(file, updated getOrElse artifact, resource)))
          case _ =>
            Logger.debug(s"Resource is still the same. Resource: $resource")
        }
    }
  }

  def handleArtifactUpdate(artifact: ArtifactLike) = {
    ArtifactDAO.update(artifact).map { lastError =>
      if (lastError.updated > 0) {
        if (lastError.updatedExisting)
          publish(ArtifactUpdated(artifact))
        else
          publish(ArtifactInserted(artifact))
      }
    }
  }

  def handleArtifactAggregation(_project: String, foundArtifacts: List[ArtifactFound]) = {
    ArtifactDAO.findAllForProject(_project).map { projectArtifacts =>
      val found = foundArtifacts.map(_.artifact)
      projectArtifacts
        .flatMap(ArtifactDAO.createArtifactFrom)
        .filterNot(found.contains)
        .map(handleArtifactDelete)

      foundArtifacts.map(a =>
        handleArtifactFound(a.originalStream, a.artifact))
    }
  }

  def handleArtifactFound(originalStream: InputStream, artifact: ArtifactLike) = {
    val resource = Resource(artifact.name, DefaultResourceTypes.DEFAULT_TYP)
    handleArtifactUpdate(artifact)
    handleResourceUpdate(originalStream, artifact, resource)
  }

  def handleArtifactDelete(artifact: ArtifactLike) = {
    ArtifactDAO.markAsDeleted(artifact).map { lastError =>
      if (lastError.updated > 0)
        publish(ArtifactUpdated(artifact))
    }
  }

  def receive = {
    case ArtifactFound(originalStream, artifact) =>
      handleArtifactFound(originalStream, artifact)

    case ArtifactDeleted(artifact) =>
      handleArtifactDelete(artifact)

    case ArtifactAggregation(_project, artifacts) =>
      handleArtifactAggregation(_project, artifacts)

    case ResourceFound(inputStream, artifact, resource) =>
      handleResourceUpdate(inputStream, artifact, resource)

    case RequestResource(artifact, resource) =>
      sender ! readFromFS(artifact.projectName, artifact.path, resource)
  }
}

object ArtifactActor extends StartableActor[ArtifactActor] {

  def name = "artifactActor"

}