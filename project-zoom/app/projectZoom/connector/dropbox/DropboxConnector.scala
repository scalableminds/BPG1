package projectZoom.connector.dropbox

import play.api.libs.concurrent.Akka
import projectZoom.util.PlayActorSystem
import akka.actor.Props
import play.api.Logger
import projectZoom.connector.Connector
import projectZoom.connector.StartAggregating

object DropboxConnector extends Connector with PlayActorSystem {
  def startAggregating = {
    DropboxAPI.create match {
      case Some(d) =>
        val actor = system.actorOf(Props(new DropboxActor(d)))
        actor ! StartAggregating
      case _ =>
        Logger.error("Couldn't start dropbox connector")
    }
  }
}