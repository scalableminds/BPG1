package projectZoom.core.event

import akka.agent.Agent
import akka.actor.Actor
import akka.actor.ActorRef
import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api.Logger
import projectZoom.util.StartableActor

trait Event
case class Unsubscribe(key: String)
case class SubscribeWithFilter(pf: PartialFunction[Any, Unit], key: String)
case class UnsubscribeAll()

class EventActor extends Actor {
  case class EventSubscription(f: PartialFunction[Event, Unit], actor: ActorRef)

  implicit val sys = context.system

  val listeners = Agent[Map[String, EventSubscription]](Map())

  def receive = {
    case Unsubscribe(key) =>
      val s = sender
      listeners.send(_ - key)
      Logger.debug("[Event] Unregistered one of: " + s.path)

    case UnsubscribeAll() =>
      val s = sender
      listeners.send(_.filter {
        case (_, EventSubscription(_, a)) => a == s
      })
      Logger.debug("[Event] Unregistered from everything: " + s.path)

    case SubscribeWithFilter(eventSelector, key) =>
      val s = sender
      listeners.send(_ + (key -> EventSubscription(eventSelector, s)))
      Logger.debug("[Event] Registered: " + s.path)

    case e: Event =>
      Logger.debug("[Event] Event: " + e.getClass.toString + " Sender: " + sender.path)
      listeners().foreach {
        case (_, EventSubscription(eventSelector, subscriber)) =>
          if (eventSelector.isDefinedAt(e))
            subscriber.forward(e)
      }
  }
}

object EventActor extends StartableActor[EventActor] {
  def name = "eventActor"
}
