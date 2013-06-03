package projectZoom.thumbnails

import projectZoom.core.event._
import projectZoom.util.StartableActor
import projectZoom.core.artifact._
import models.ArtifactLike
import models.ResourceLike
import models.DefaultResourceTypes
import java.io.File
import scala.collection.JavaConversions._
import projectZoom.thumbnails.all._

trait ThumbnailActor extends EventSubscriber with EventPublisher {

  def receive = {
    case ResourceUpdated(file, artifact, resource) =>
      handleResourceUpdate(file, artifact, resource)
    case ResourceInserted(file, artifact, resource) =>
      handleResourceUpdate(file, artifact, resource)
  }
  
  def handleResourceUpdate(file: File, artifact: ArtifactLike, resource: ResourceLike)
}
