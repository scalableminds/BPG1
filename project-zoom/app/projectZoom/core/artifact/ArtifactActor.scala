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

case class UpdateInfo(origin: String, projectName: String)

trait ArtifactUpdate extends Event
case class ArtifactFound(originalStream: InputStream, artifact: ArtifactInfo) extends ArtifactUpdate
case class ArtifactDeleted(artifact: ArtifactInfo) extends ArtifactUpdate
case class ArtifactAggregation(_project: String, l: List[ArtifactFound]) extends ArtifactUpdate

case class ArtifactUpdated(artifact: ArtifactInfo) extends Event
case class ArtifactInserted(artifact: ArtifactInfo) extends Event

case class ResourceFound(inputStream: InputStream, artifact: ArtifactInfo, resource: ResourceInfo) extends Event

case class ResourceUpdated(resource: ResourceInfo) extends Event
case class ResourceInserted(resource: ResourceInfo) extends Event

case class RequestResource(_project: String, resource: ResourceInfo)

trait FSWriter {
  val basePath = Play.current.configuration.getString("core.resource.basePath") getOrElse "data"

  def pathFor(project: String, typ: String, fileName: String) =
    s"$basePath/$project/$typ/$fileName"

  def fileFor(project: String, typ: String, fileName: String) = {
    val path = pathFor(project, typ, fileName)
    val file = new File(path)
    if (file.getAbsolutePath().startsWith(basePath))
      Some(file -> path)
    else
      None
  }

  def writeToFS(is: InputStream, _project: String, resourceInfo: ResourceInfo) = {
    fileFor(_project, resourceInfo.typ, resourceInfo.fileName).map {
      case (file, path) =>
        val os = new FileOutputStream(file)
        val writtenBytes = org.apache.commons.io.IOUtils.copyLarge(is, os)
        is.available()
        os.close()
        file -> path
    }
  }

  def readFromFS(_project: String, resourceInfo: ResourceInfo): Option[InputStream] = {
    fileFor(_project, resourceInfo.typ, resourceInfo.fileName).map {
      case (file, _) =>
        new FileInputStream(file)
    }
  }
}

class ArtifactActor extends EventSubscriber with EventPublisher with FSWriter {

  def handleResourceUpdate(is: InputStream, artifactInfo: ArtifactInfo, resourceInfo: ResourceInfo) = {
    writeToFS(is, artifactInfo._project, resourceInfo).map {
      case (file, path) =>
        val hash = DigestUtils.md5Hex(FileUtils.readFileToByteArray(file))
        ArtifactDAO.insertRessource(artifactInfo)(path, hash, resourceInfo).map { lastError =>
          if (lastError.updated > 0) {
            if (lastError.updatedExisting)
              publish(ResourceUpdated(resourceInfo))
            else
              publish(ResourceInserted(resourceInfo))
          }
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

    case RequestResource(_project, resourceInfo) =>
      sender ! readFromFS(_project, resourceInfo)
  }
}

object ArtifactActor extends StartableActor[ArtifactActor] {

  def name = "artifactActor"

}