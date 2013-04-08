package projectZoom.connector.incom

import projectZoom.connector.Connector
import projectZoom.util.PlayActorSystem
import projectZoom.connector.StartAggregating
import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api.Logger

class IncomConnector extends Connector with PlayActorSystem {
  val username = "test"
  val password = "miep"
  
  def startAggregating(implicit app: play.api.Application) = {
    IncomAPI.create(username, password) match {
      case Some(api) =>
        val actor = Akka.system(app).actorOf(Props(new IncomActor(api)))
        actor ! StartAggregating
      case _ =>
        Logger.error("Couldn't start incom connector")
    }
  }
}