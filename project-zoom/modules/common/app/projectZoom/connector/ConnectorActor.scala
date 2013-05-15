package projectZoom.connector

import akka.actor.Actor
import projectZoom.core.artifact.ArtifactUpdate
import play.api.Logger

case class StartAggregating()
case class Aggregate()

trait ConnectorInterface{
  def start()
  def aggregate()
}

trait ConnectorActor extends Actor with ConnectorInterface{
  lazy val artifactActor = {
    val a = context.system.actorFor("ArtifactActor")
    Logger.debug("artifact actor: " + a.path)
    a
  }
    
  def receive = {
    case StartAggregating =>
      start()
    case Aggregate =>
      aggregate()
      
    case update : ArtifactUpdate =>
      
  }
}