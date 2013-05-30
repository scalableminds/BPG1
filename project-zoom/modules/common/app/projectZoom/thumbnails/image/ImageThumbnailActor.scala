package projectZoom.thumbnails.image

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

class ImageThumbnailActor extends ThumbnailActor {
  
  lazy val thumbnailPlugin = new ImageThumbnailPlugin()

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

object ImageThumbnailActor extends StartableActor[ImageThumbnailActor]{
  
  def name = "imageThumbnailActor"    
}