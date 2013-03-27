package projectZoom.event

import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Actor

trait EventPublisher{ this: Actor =>
  lazy val eventSystem = Akka.system.actorFor(s"/user/${EventActor.name}")
  
  def publish[T <: Event](event: T) {
    eventSystem ! event
  }
}