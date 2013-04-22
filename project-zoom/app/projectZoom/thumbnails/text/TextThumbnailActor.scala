package projectZoom.thumbnails.text
import play.api.Logger
import projectZoom.core.event._
import projectZoom.thumbnails.text.TextThumbnailPlugin
import projectZoom.util.StartableActor

class TextThumbnailActor extends EventSubscriber with EventPublisher{
  
  def receive = {
    case x =>
    //  Logger.debug("Thumbnail Actor received: " + x.toString + " s: " + sender.path)
  }
  
  val textThumbnailPlugin = new TextThumbnailPlugin()
  
  
}

object TextThumbnailActor extends StartableActor[TextThumbnailActor]{
  
  def name = "textThumbnailActor"    
}