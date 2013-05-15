package projectZoom.util

import akka.actor.ActorRef
import play.api.libs.concurrent.Akka
import akka.actor.Props
import akka.actor.Actor
import scala.reflect.ClassTag

trait StartableActor[T <: Actor] {
  def name: String

  def postStart(actor: ActorRef) {

  }

  def start(implicit sys: akka.actor.ActorSystem, tag: ClassTag[T]) = {
    val actor = sys.actorOf(Props[T], name)
    postStart(actor)
    actor
  }
}