package projectZoom.thumbnails

import akka.actor.Actor
import play.api.libs.concurrent.Akka
import play.api.Logger
import projectZoom.core.event._
import akka.actor.Props
import projectZoom.util.StartableActor

class ThumbnailActor extends EventSubscriber with EventPublisher{
  
  def receive = {
    case x =>
      Logger.debug("Thumbnail Actor received: " + x.toString + " s: " + sender.path)
  }
  
}

object ThumbnailActor extends StartableActor[ThumbnailActor]{
  
  def name = "thumbnailActor"    
}