package projectZoom.connector

import akka.actor._
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._
import projectZoom.core.event.EventSubscriber
import projectZoom.connector.Filemaker._
import projectZoom.util.SSH
import projectZoom.util.{PlayActorSystem, StartableActor}
import play.api.Logger

class CreatingTunnelFailed extends RuntimeException

class SupervisorActor extends EventSubscriber with PlayActorSystem{
  
  override def preStart {
    super.preStart
    FilemakerConnector.startAggregating(context)
  }
  
  override def receive = {
    case _ => println("TODO")
  }
  
  val connectors = List[ActorRef]()
  
  def updateProjects = {
    
  }
  
  def updateUsers = {
    
  }
  
  def updateSettings = {
    
  }
  
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 minute) {
    case _ => Restart
  }
}

object SupervisorActor extends StartableActor[SupervisorActor]
{
  def name = "supervisorActor"
}