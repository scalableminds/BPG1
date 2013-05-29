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

class TextThumbnailActor extends ThumbnailActor {
  
  lazy val thumbnailPlugin = new TextThumbnailPlugin()

  def handleResourceUpdate(resource: File, artifactInfo: ArtifactInfo, resourceInfo: ResourceInfo) {
    if (resourceInfo.typ == DefaultResourceTypes.DEFAULT_TYP) {
      
      val tempFiles = thumbnailPlugin.onResourceFound(resource, resourceInfo)
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