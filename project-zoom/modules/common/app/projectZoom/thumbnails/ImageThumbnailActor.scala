package projectZoom.thumbnails
import play.api.Logger
import projectZoom.core.event._
import projectZoom.util.StartableActor

class IconActor extends EventSubscriber with EventPublisher{
  
  def receive = {
    case x =>
    //  Logger.debug("Thumbnail Actor received: " + x.toString + " s: " + sender.path)
  }
  
  val iconPlugin = new IconPlugin()
  
  
}

object IconActor extends StartableActor[IconActor]{
  
  def name = "iconActor"    
}