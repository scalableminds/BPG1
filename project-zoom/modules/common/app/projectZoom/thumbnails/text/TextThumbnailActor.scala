package projectZoom.thumbnails.text
import play.api.Logger
import projectZoom.core.event._
import projectZoom.util.StartableActor
import projectZoom.core.artifact.ResourceInserted
import scala.collection.JavaConversions._

class TextThumbnailActor extends EventSubscriber with EventPublisher{
  
  def receive = {
    case ResourceInserted(resourceInfo) =>
    //  Logger.debug("Thumbnail Actor received: " + x.toString + " s: " + sender.path)
        val artifacts = textThumbnailPlugin.onResourceFound(resourceInfo);
        /*
        artifacts.map{artifact =>
          publish(ArtifactFound(InputStream, Artifa))
        }
        
        */
  }
  
  val textThumbnailPlugin = new TextThumbnailPlugin()

  
}

object TextThumbnailActor extends StartableActor[TextThumbnailActor]{
  
  def name = "textThumbnailActor"    
}