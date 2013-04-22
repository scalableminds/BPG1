package projectZoom.util

import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.concurrent.ExecutionContext

trait PlayActorSystem {

  implicit val system = Akka.system

  def userActorFor(name: String) =
    system.actorFor(s"/user/$name")
}