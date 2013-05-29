package projectZoom.thumbnails.text

import play.api.Logger
import projectZoom.core.event._
import projectZoom.util.StartableActor
import projectZoom.core.artifact._
import projectZoom.thumbnails._
import models.ArtifactInfo
import models.ResourceInfo
import models.DefaultResourceTypes
import java.io.File
import scala.collection.JavaConversions._

class TextThumbnailActor extends EventSubscriber with EventPublisher{
  
  lazy val textThumbnailPlugin = new TextThumbnailPlugin()
  
  def receive = {
    case ResourceUpdated(resource, artifactInfo, resourceInfo) =>
      handleResourceUpdate(resource, artifactInfo, resourceInfo)
    case ResourceInserted(resource, artifactInfo, resourceInfo) =>
      handleResourceUpdate(resource, artifactInfo, resourceInfo)
  }
  
  def handleResourceUpdate(resource: File, artifactInfo: ArtifactInfo, resourceInfo: ResourceInfo) {
    if (resourceInfo.typ == DefaultResourceTypes.DEFAULT_TYP) {
      
      val tempFiles = textThumbnailPlugin.onResourceFound(resource, resourceInfo)
      tempFiles.map { tempFile =>
        val iconResource = ResourceInfo(
        name = tempFile.getName(),
        typ = tempFile.getType())
        publish(ResourceFound(tempFile.getStream(), artifactInfo, iconResource))
      }
    }
  }
}

object TextThumbnailActor extends StartableActor[TextThumbnailActor]{
  
  def name = "textThumbnailActor"    
}