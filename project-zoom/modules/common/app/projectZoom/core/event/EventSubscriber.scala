package projectZoom.core.event

import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Actor
import java.util.UUID

trait EventSubscriber extends Actor {

  lazy val eventActor = Akka.system.actorFor(s"/user/${EventActor.name}")

  def receive: PartialFunction[Any, Unit]
  
  def unsubscribe(key: String){
    eventActor ! Unsubscribe(key)
  }

    def unsubscribeAll(){
    eventActor ! UnsubscribeAll()
  }
  
  def subscribe(f: PartialFunction[Any, Unit]) = {
    val key = UUID.randomUUID().toString
    eventActor ! SubscribeWithFilter(f, key)
    key
  }
  
  override def preStart() {
    subscribe(receive)
  }
}