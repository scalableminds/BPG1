package projectZoom.util

import akka.actor.ActorRef
import play.api.libs.concurrent.Akka
import akka.actor.Props
import akka.actor.Actor
import scala.reflect.ClassTag

trait StartableActor[T <: Actor] {
  def name: String
  def start(implicit app: play.api.Application, tag: ClassTag[T]) =
    Akka.system(app).actorOf(Props[T], name)
}