package projectZoom.core.event

import akka.agent.Agent
import akka.actor.Actor
import akka.actor.ActorRef
import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api.Logger
import projectZoom.util.StartableActor

trait Event
case class UnRegister()
case class RegisterAsHandlerWithFilter(pf: PartialFunction[Any, Unit])

class EventActor extends Actor {

  implicit val sys = context.system

  val listeners = Agent[Map[PartialFunction[Event, Unit], ActorRef]](Map())

  def receive = {
    case UnRegister() =>
      val s = sender
      listeners.send(_.filter(_._2 == s))
      Logger.debug("[Event] Unregistered: " + s.path)
    case RegisterAsHandlerWithFilter(eventSelector) =>
      val s = sender
      listeners.send(_ + (eventSelector -> s))
      Logger.debug("[Event] Registered: " + s.path)
    case e: Event =>
      Logger.debug("[Event] Event: " + e + " Sender: " + sender.path)
      listeners().foreach {
        case (eventSelector, s) =>
          if (eventSelector.isDefinedAt(e))
            s.forward(e)
      }
  }
}

object EventActor extends StartableActor[EventActor]{
  def name = "eventActor"
}
