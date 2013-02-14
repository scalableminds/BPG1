package projectZoom.util

import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.concurrent.ExecutionContext

trait PlayActorSystem {
  implicit val ec: ExecutionContext = 
    play.api.libs.concurrent.Execution.Implicits.defaultContext
  implicit val system = Akka.system
}