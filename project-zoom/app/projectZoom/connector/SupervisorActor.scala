package projectZoom

import akka.actor._
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._
import projectZoom.core.event.EventSubscriber

import projectZoom.util.PlayActorSystem

class SupervisorActor extends EventSubscriber with PlayActorSystem{
  
  def receive = {
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