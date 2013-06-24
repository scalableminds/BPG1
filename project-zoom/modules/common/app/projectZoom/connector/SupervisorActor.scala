package projectZoom.connector

import akka.actor._
import akka.agent._
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._
import projectZoom.core.event.EventSubscriber
import projectZoom.connector.Filemaker._
import projectZoom.connector.box._
import projectZoom.util.{ PlayActorSystem, StartableActor, PlayConfig }
import play.api.Logger
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import projectZoom.core.event._
import akka.pattern.gracefulStop
import scala.concurrent._
import scala.util.{Try, Success, Failure}

case object UpdateProjects

case class BoxUpdated(accessTokens: BoxAccessTokens) extends Event


class SupervisorActor extends EventSubscriber with PlayActorSystem with PlayConfig {

  val BoxActor = context.actorOf(Props[BoxActor], "BoxActor")
  val FilemakerActor = context.actorOf(Props[FilemakerActor], "FilemakerActor")
  
  //val test = context.actorOf(TestActor.props)
  
  Future(updateProjects).onComplete{
    case Success(_) => 
      FilemakerActor ! StartAggregating
      BoxActor ! StartAggregating    
    case Failure(err) => Logger.error(s"Supervisor failed creating Project cache:\n$err")
  }
  
  def stopActor(actorOpt: Option[ActorRef]): Unit = actorOpt.foreach{actor => stopActor(actor)}
  
  def stopActor(actor: ActorRef) {
    val actorName = actor.path.name
    actorName match {
      case "BoxActor" => DBProxy.deleteBoxEventStreamPos
      case _ => 
    }
    Try{
      val stopped = gracefulStop(actor, 10 seconds)
      Await.result(stopped, 10 seconds)
      } match {
        case Success(_) => Logger.debug(s"${actor.path.name} stopped gracefully")
        case Failure(err) => 
          Logger.error(s"Failed to stop ${actor.path.name}:\n${err}")
          //maybe restart self instead
          actor ! PoisonPill
      } 
  } 
  
/*  def restartActor(actor: ActorRef) = {
    val actorName = actor.path.name
    Logger.debug(s"restarting actor $actorName")
    actorName match {
      case "BoxActor" => startBoxActor 
      case _ => 
    }
  }*/

  override def receive = {
    case UpdateProjects => updateProjects
    case BoxUpdated(accessTokens) => stopActor(BoxActor)
    //case Terminated(actor) => restartActor(actor)  
  }

  def updateProjects =
    DBProxy.getProjects.map{projects =>
      ProjectCache.setProjects(projects)
    }

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 minute) {
    case _ => Restart
  }
  
}

object SupervisorActor extends StartableActor[SupervisorActor] {
  def name = "supervisorActor"
}