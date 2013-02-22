package projectZoom.artifact

import akka.actor.Actor
import java.io.File
import play.api.libs.json.JsValue
import projectZoom.util.DBCollection
import java.io.InputStream
import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api.Logger

case class UpdateInfo(origin: String, projectName: String)

trait ArtifactUpdate
case class UpdateFileArtifact(info: UpdateInfo, path: String, fileStream: InputStream, metadata: JsValue) extends ArtifactUpdate
case class DeleteFileArtifact(info: UpdateInfo, path: String) extends ArtifactUpdate

class ArtifactActor(rootDirectory: String) extends Actor{
  def receive = {
    //case UpdateFileArtifact(info, path, file, metadata) =>
      
    case x =>
      Logger.debug("Artifact Actor received: " + x.toString)
  }
}

object ArtifactActor{
  def start(implicit app: play.api.Application) = 
    Akka.system(app).actorOf(Props(new ArtifactActor("")), "ArtifactActor")
}