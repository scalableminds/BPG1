package projectZoom.core.event

import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Actor

/**
 * If a class wants to emit events it can use this trait to easily grab an instance of the event actor
 */
trait EventPublisher{
  lazy val eventSystem = Akka.system.actorFor(s"/user/${EventActor.name}")
  
  def publish[T <: Event](event: T) {
    eventSystem ! event
  }
}