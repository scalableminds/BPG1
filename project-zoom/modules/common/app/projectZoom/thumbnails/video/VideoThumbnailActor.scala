package projectZoom.thumbnails.video
import play.api.Logger
import projectZoom.core.event._
import projectZoom.util.StartableActor

class VideoThumbnailActor extends EventSubscriber with EventPublisher{
  
  def receive = {
    case x =>
    //  Logger.debug("Thumbnail Actor received: " + x.toString + " s: " + sender.path)
  }
  
  val videoThumbnailPlugin = new VideoThumbnailPlugin()
  
  
}

object VideoThumbnailActor extends StartableActor[VideoThumbnailActor]{
  
  def name = "videoThumbnailActor"    
}