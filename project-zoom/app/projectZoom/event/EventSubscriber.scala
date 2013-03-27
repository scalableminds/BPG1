package projectZoom.event

import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Actor

trait EventSubscriber extends Actor {

  lazy val eventActor = Akka.system.actorFor(s"/user/${EventActor.name}")

  def receive: PartialFunction[Any, Unit]

  def registerAtEventActor(f: PartialFunction[Any, Unit]) {
    eventActor ! RegisterAsHandlerWithFilter(f)
  }

  override def preStart() {
    registerAtEventActor(receive)
  }
}