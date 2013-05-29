package projectZoom.thumbnails

import projectZoom.core.event._
import projectZoom.util.StartableActor
import projectZoom.core.artifact._
import models.ArtifactInfo
import models.ResourceInfo
import models.DefaultResourceTypes
import java.io.File
import scala.collection.JavaConversions._
import projectZoom.thumbnails.all._

class ThumbnailActor extends EventSubscriber with EventPublisher {

  def receive = {
    case ResourceUpdated(resource, artifactInfo, resourceInfo) =>
      handleResourceUpdate(resource, artifactInfo, resourceInfo)
    case ResourceInserted(resource, artifactInfo, resourceInfo) =>
      handleResourceUpdate(resource, artifactInfo, resourceInfo)
  }
  
  def handleResourceUpdate(resource: File, artifactInfo: ArtifactInfo, resourceInfo: ResourceInfo) {}
}
