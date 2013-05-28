package projectZoom.thumbnails.image
import play.api.Logger
import projectZoom.core.event._
import projectZoom.util.StartableActor

class ImageThumbnailActor extends EventSubscriber with EventPublisher{
  
  def receive = {
    case x =>
    //  Logger.debug("Thumbnail Actor received: " + x.toString + " s: " + sender.path)
  }
  
  val videoThumbnailPlugin = new ImageThumbnailPlugin()
  
  
}

object ImageThumbnailActor extends StartableActor[ImageThumbnailActor]{
  
  def name = "imageThumbnailActor"    
}