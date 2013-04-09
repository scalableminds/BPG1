package projectZoom.thumbnails

import akka.actor.Actor
import play.api.libs.concurrent.Akka
import play.api.Logger
import projectZoom.core.event._
import akka.actor.Props
import projectZoom.util.StartableActor

class TextThumbnailActor extends EventSubscriber with EventPublisher{
  
  def receive = {
    case x =>
      Logger.debug("Thumbnail Actor received: " + x.toString + " s: " + sender.path)
  }
  
  val textThumbnailPlugin = new TextThumbnailJavaPlugin()
  
  
}

object TextThumbnailActor extends StartableActor[TextThumbnailActor]{
  
  def name = "textThumbnailActor"    
}