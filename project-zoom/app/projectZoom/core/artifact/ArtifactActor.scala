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
import models.Artifact
import models.ArtifactInfo
import models.ResourceInfo
import play.api.Play
import java.io.FileOutputStream
import java.io.FileInputStream
import models.Resource

case class UpdateInfo(origin: String, projectName: String)

trait ArtifactUpdate extends Event
case class ArtifactFound(fileStream: InputStream, arifact: ArtifactInfo) extends ArtifactUpdate
case class ArtifactDeleted(artifact: ArtifactInfo) extends ArtifactUpdate
case class ArtifactAggregation(l: List[ArtifactFound]) extends ArtifactUpdate

case class ArtifactUpdated(artifact: ArtifactInfo) extends Event
case class ArtifactInserted(artifact: ArtifactInfo) extends Event

case class ResourceUpdated(resource: ResourceInfo) extends Event
case class ResourceInserted(resource: ResourceInfo) extends Event

trait FSWriter {
  val basePath = Play.current.configuration.getString("core.resource.basePath") getOrElse "data"

  def pathFor(project: String, typ: String, fileName: String) =
    s"$basePath/$project/$typ/$fileName"

  def fileFor(project: String, typ: String, fileName: String) = {
    val file = new File(pathFor(project, typ, fileName))
    if (file.getAbsolutePath().startsWith(basePath))
      Some(file)
    else
      None
  }

  def writeToFS(is: InputStream, _project: String, resourceInfo: ResourceInfo) = {
    fileFor(_project, resourceInfo.typ, resourceInfo.fileName).map { f =>
      val os = new FileOutputStream(f)
      val writtenBytes = org.apache.commons.io.IOUtils.copyLarge(is, os)
      is.available()
      os.close()
      writtenBytes
    } getOrElse 0
  }

  def readFromFS(_project: String, resourceInfo: ResourceInfo): Option[InputStream] = {
    fileFor(_project, resourceInfo.typ, resourceInfo.fileName).map { f =>
      new FileInputStream(f)
    }
  }
}

class ArtifactActor extends EventSubscriber with EventPublisher with FSWriter {

  def receive = {
    case ArtifactFound(inputStream, foundArtifact) =>
      Artifact.update(foundArtifact)
      val resourceInfo = ResourceInfo(foundArtifact.name , Resource.DEFAULT_TYP)
      writeToFS(inputStream, foundArtifact._project, resourceInfo)
      publish(ArtifactUpdated(foundArtifact))
      publish(ResourceUpdated(resourceInfo))
    case ArtifactDeleted(artifactInfo) =>
      Artifact.markAsDeleted(artifactInfo)
    case x =>
      Logger.debug("Artifact Actor received: " + x.toString + " s: " + sender.path)
  }
}

object ArtifactActor extends StartableActor[ArtifactActor] {

  def name = "artifactActor"

}