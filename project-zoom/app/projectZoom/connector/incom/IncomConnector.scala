package projectZoom.connector.incom

import projectZoom.connector.Connector
import projectZoom.util.PlayActorSystem
import projectZoom.connector.StartAggregating
import play.api.libs.concurrent.Akka
import akka.actor._
import play.api.Logger

class IncomConnector extends Connector with PlayActorSystem {
  val username = "test"
  val password = "miep"
  
  def startAggregating(context: ActorRefFactory) = {
    IncomAPI.create(username, password) match {
      case Some(api) =>
        val actor = context.actorOf(Props(new IncomActor(api)))
        actor ! StartAggregating
      case _ =>
        Logger.error("Couldn't start incom connector")
    }
  }
}