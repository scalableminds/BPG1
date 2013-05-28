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
import models.ArtifactInfo
import models.Artifact
import models.ResourceInfo
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
case class ArtifactFound(originalStream: InputStream, artifact: ArtifactInfo) extends ArtifactUpdate
case class ArtifactDeleted(artifact: ArtifactInfo) extends ArtifactUpdate
case class ArtifactAggregation(_project: String, l: List[ArtifactFound]) extends ArtifactUpdate

case class ResourceFound(inputStream: InputStream, artifact: ArtifactInfo, resource: ResourceInfo) extends Event

/*
 * Publishes
 */
case class ArtifactUpdated(artifact: ArtifactInfo) extends Event
case class ArtifactInserted(artifact: ArtifactInfo) extends Event

case class ResourceUpdated(file: File, artifact: ArtifactInfo, resource: ResourceInfo) extends Event
case class ResourceInserted(file: File, artifact: ArtifactInfo, resource: ResourceInfo) extends Event

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

  def writeToFS(is: InputStream, projectName: String, artifactPath: String, resourceInfo: ResourceInfo) = {
    fileFor(projectName, artifactPath, resourceInfo).map {
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

  def handleResourceUpdate(is: InputStream, artifactInfo: ArtifactInfo, resourceInfo: ResourceInfo) = {
    writeToFS(is, artifactInfo.projectName, artifactInfo.path, resourceInfo).map {
      case file =>
        val hash = DigestUtils.md5Hex(FileUtils.readFileToByteArray(file))
        ArtifactDAO.findResource(artifactInfo, resourceInfo).map {
          case None =>
            ArtifactDAO.insertResource(artifactInfo)(hash, resourceInfo).map(_ =>
              publish(ResourceInserted(file, artifactInfo, resourceInfo)))
          case Some(r) /*if r.hash != hash*/ =>
            // TODO: remove comment
            ArtifactDAO.updateHashOfResource(artifactInfo)(hash, resourceInfo).map(_ =>
              publish(ResourceUpdated(file, artifactInfo, resourceInfo)))
          case _ =>
            Logger.debug(s"Resource is still the same. Resource: $resourceInfo")
        }
    }
  }

  def handleArtifactUpdate(artifactInfo: ArtifactInfo) = {
    ArtifactDAO.update(artifactInfo).map { lastError =>
      if (lastError.updated > 0) {
        if (lastError.updatedExisting)
          publish(ArtifactUpdated(artifactInfo))
        else
          publish(ArtifactInserted(artifactInfo))
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

  def handleArtifactFound(originalStream: InputStream, artifactInfo: ArtifactInfo) = {
    val resourceInfo = ResourceInfo(artifactInfo.name, DefaultResourceTypes.DEFAULT_TYP)
    handleArtifactUpdate(artifactInfo)
    handleResourceUpdate(originalStream, artifactInfo, resourceInfo)
  }

  def handleArtifactDelete(artifactInfo: ArtifactInfo) = {
    ArtifactDAO.markAsDeleted(artifactInfo).map { lastError =>
      if (lastError.updated > 0)
        publish(ArtifactUpdated(artifactInfo))
    }
  }

  def receive = {
    case ArtifactFound(originalStream, artifactInfo) =>
      handleArtifactFound(originalStream, artifactInfo)

    case ArtifactDeleted(artifactInfo) =>
      handleArtifactDelete(artifactInfo)

    case ArtifactAggregation(_project, artifacts) =>
      handleArtifactAggregation(_project, artifacts)

    case ResourceFound(inputStream, artifactInfo, resourceInfo) =>
      handleResourceUpdate(inputStream, artifactInfo, resourceInfo)

    case RequestResource(artifactInfo, resourceInfo) =>
      sender ! readFromFS(artifactInfo.projectName, artifactInfo.path, resourceInfo)
  }
}

object ArtifactActor extends StartableActor[ArtifactActor] {

  def name = "artifactActor"

}