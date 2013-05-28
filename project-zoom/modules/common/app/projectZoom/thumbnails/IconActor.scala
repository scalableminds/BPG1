package projectZoom.thumbnails
import play.api.Logger
import projectZoom.core.event._
import projectZoom.util.StartableActor
import projectZoom.core.artifact._
import models.ArtifactInfo
import models.ResourceInfo
import models.DefaultResourceTypes
import java.io.File

class IconActor extends EventSubscriber with EventPublisher {

  lazy val iconPlugin = new IconPlugin()

  def receive = {
    case ResourceUpdated(resource, artifactInfo, resourceInfo) =>
      handleResourceUpdate(resource, artifactInfo, resourceInfo)
    case ResourceInserted(resource, artifactInfo, resourceInfo) =>
      handleResourceUpdate(resource, artifactInfo, resourceInfo)

    //  Logger.debug("Thumbnail Actor received: " + x.toString + " s: " + sender.path)
  }
  
  def handleResourceUpdate(resource: File, artifactInfo: ArtifactInfo, resourceInfo: ResourceInfo) {
    if (resourceInfo.typ == DefaultResourceTypes.DEFAULT_TYP) {
      val iconStream = iconPlugin.onResourceFound(resource, resourceInfo)
      val iconResource = ResourceInfo(
        name = resourceInfo.name.split("\\.").dropRight(1).mkString(".") + ".ico",
        typ = "icon")
      publish(ResourceFound(iconStream, artifactInfo, iconResource))
    }
  }
}

object IconActor extends StartableActor[IconActor] {

  def name = "iconActor"
}