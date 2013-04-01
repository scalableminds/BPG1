package projectZoom

import akka.actor._
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._

import scala.collection.mutable.ListBuffer

object SupervisorActor {
  case object ProjectInserted
  case object UserInserted
  case object SettingsUpdated
}

class SupervisorActor extends Actor {
  import SupervisorActor._
  
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