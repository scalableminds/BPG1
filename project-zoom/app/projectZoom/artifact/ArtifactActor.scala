package projectZoom.artifact

import akka.actor.Actor
import java.io.File
import play.api.libs.json.JsValue
import projectZoom.util.DBCollection

case class UpdateInfo(origin: String, projectName: String)

case class UpdateFileArtifact(info: UpdateInfo, path: String, file: File, metadata: JsValue)

class ArtifactActor(rootDirectory: String) extends Actor{
  
  def receive = {
    case UpdateFileArtifact(info, path, file, metadata) =>
      
  }

}