package projectZoom.connector

import akka.actor.Actor

case class StartAggregating()

trait ConnectorInterface{
  def start()
}

trait ConnectorActor extends Actor with ConnectorInterface{
  
  def receive = {
    case StartAggregating =>
      start()
  }
}