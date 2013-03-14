package projectZoom.connector.incom

import projectZoom.connector.Connector
import projectZoom.util.PlayActorSystem
import projectZoom.connector.StartAggregating
import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api.Logger

class IncomConnector extends Connector with PlayActorSystem {
  def startAggregating = {
    IncomAPI.create match {
      case Some(d) =>
        val actor = system.actorOf(Props(new IncomActor(d)))
        actor ! StartAggregating
      case _ =>
        Logger.error("Couldn't start incom connector")
    }
  }
}