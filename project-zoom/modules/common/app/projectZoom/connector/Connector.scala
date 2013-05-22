package projectZoom.connector

import akka.actor.ActorRefFactory

trait Connector {
  def startAggregating(context: ActorRefFactory)
}