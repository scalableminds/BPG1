package projectZoom

import akka.actor._
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._

import projectZoom.util.PlayActorSystem

object SupervisorActor {
  case object ProjectInserted
  case object UserInserted
  case object SettingsUpdated
}

class SupervisorActor extends Actor with PlayActorSystem{
  import SupervisorActor._
  
  val connectors = List[ActorRef]()
  
  def receive = {
    case ProjectInserted => updateProjects
    case UserInserted => updateUsers
    case SettingsUpdated => updateSettings
  }
  
  override def preStart = {
    self ! ProjectInserted
    self ! UserInserted
    self ! SettingsUpdated
  }
  
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