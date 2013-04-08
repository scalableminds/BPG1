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

case class UpdateInfo(origin: String, projectName: String)

trait ArtifactUpdate
case class UpdateFileArtifact(info: UpdateInfo, path: String, fileStream: InputStream, metadata: JsValue) extends ArtifactUpdate
case class DeleteFileArtifact(info: UpdateInfo, path: String) extends ArtifactUpdate

class ArtifactActor extends EventSubscriber with EventPublisher{
  
  def receive = {
    //case UpdateFileArtifact(info, path, file, metadata) =>
     
    case x =>
      Logger.debug("Artifact Actor received: " + x.toString + " s: " + sender.path)
  }
}

object ArtifactActor extends StartableActor[ArtifactActor]{

  def name = "artifactActor"

}