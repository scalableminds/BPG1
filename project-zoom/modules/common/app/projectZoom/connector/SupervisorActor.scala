package projectZoom.connector

import akka.actor._
import akka.agent._
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._
import projectZoom.core.event.EventSubscriber
import projectZoom.connector.Filemaker._
import projectZoom.connector.box._
import projectZoom.util.SSH
import projectZoom.util.{ PlayActorSystem, StartableActor, PlayConfig }
import play.api.Logger
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import box.{ UpdateBoxAccessTokens, BoxAccessTokens }
import projectZoom.core.event._
import akka.pattern.gracefulStop
import scala.concurrent._
import scala.util.{Try, Success, Failure}

case object UpdateProjects

class CreatingTunnelFailed extends RuntimeException

case class BoxUpdated(accessTokens: BoxAccessTokens) extends Event


class SupervisorActor extends EventSubscriber with PlayActorSystem with PlayConfig {

  val BoxActor = Agent[Option[ActorRef]](None)
  
  Future(updateProjects).onSuccess{
    case _ => 
      FilemakerConnector.startAggregating(context)
      startBoxActor
  }

  def startBoxActor = {
    for {accessTokenOpt <-  DBProxy.getBoxTokens
         streamPosOpt <- DBProxy.getBoxEventStreamPos} {
      for {
        client_id <- config.getString("box.client_id")
        client_secret <- config.getString("box.client_secret")
        accessTokens <- accessTokenOpt
        streamPos <- streamPosOpt orElse Some(0.toLong)
      } {
        val newBoxActor = context.actorOf(Props(new BoxActor(BoxAppKeyPair(client_id, client_secret), accessTokens, streamPos)), "BoxActor")
        BoxActor.send(Some(newBoxActor))
        context.watch(newBoxActor)
      }
    }
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
  
  def restartActor(actor: ActorRef) = {
    val actorName = actor.path.name
    Logger.debug(s"restarting actor $actorName")
    actorName match {
      case "BoxActor" => startBoxActor 
      case _ => 
    }
  }
  
  override def preStart {
    super.preStart
  }

  override def receive = {
    case UpdateBoxAccessTokens(tokens: BoxAccessTokens) => DBProxy.setBoxToken(tokens)
    case UpdateProjects => updateProjects
    case BoxUpdated(accessTokens) => stopActor(BoxActor())
    case Terminated(actor) => restartActor(actor)  
  }

  val connectors = List[ActorRef]()

  def updateProjects =
    DBProxy.getProjects.map{projects =>
      ProjectCache.setProjects(projects)
    }

  def updateUsers = {

  }

  def updateSettings = {

  }

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 minute) {
    case _ => Restart
  }
}

object SupervisorActor extends StartableActor[SupervisorActor] {
  def name = "supervisorActor"
}