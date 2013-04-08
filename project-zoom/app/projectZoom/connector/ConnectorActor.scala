package projectZoom.connector

import akka.actor.Actor
import projectZoom.core.artifact.ArtifactUpdate
import play.api.Logger

case class StartAggregating()

trait ConnectorInterface{
  def start()
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
      
    case update : ArtifactUpdate =>
      
  }
}