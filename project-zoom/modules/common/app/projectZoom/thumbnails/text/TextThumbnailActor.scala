package projectZoom.thumbnails.text

import play.api.Logger
import projectZoom.core.event._
import projectZoom.util.StartableActor
import projectZoom.core.artifact._
import projectZoom.thumbnails._
import models.ArtifactLike
import models.ResourceLike
import models.DefaultResourceTypes
import java.io.File
import scala.collection.JavaConversions._
import models.Resource

class TextThumbnailActor extends ThumbnailActor {
  
  lazy val thumbnailPlugin = new TextThumbnailPlugin()

  override def handleResourceUpdate(file: File, artifact: ArtifactLike, resource: ResourceLike) {
    if (resource.typ == DefaultResourceTypes.DEFAULT_TYP) {
      
      val tempFiles = thumbnailPlugin.onResourceFound(file, resource)
      tempFiles.map { tempFile =>
        val iconResource = Resource(
        name = tempFile.getName(),
        typ = tempFile.getType())
        publish(ResourceFound(tempFile.getStream(), artifact, iconResource))
      }
    }
  }
}

object TextThumbnailActor extends StartableActor[TextThumbnailActor]{
  
  def name = "textThumbnailActor"    
}