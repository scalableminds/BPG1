package projectZoom.connector

import akka.actor.ActorRef

trait Connector {
  def startAggregating(implicit app: play.api.Application)
}