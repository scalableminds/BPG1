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

case object UpdateProjects

class CreatingTunnelFailed extends RuntimeException

class SupervisorActor extends EventSubscriber with PlayActorSystem with PlayConfig {

  val BoxActor = Agent[Option[ActorRef]](None)

  def startBoxActor = {
    for {accessTokenOpt <-  DBProxy.getBoxTokens
         streamPosOpt <- DBProxy.getBoxEventStreamPos} {
      for {
        client_id <- config.getString("box.client_id")
        client_secret <- config.getString("box.client_secret")
        accessTokens <- accessTokenOpt
        streamPos <- streamPosOpt orElse Some(0.toLong)
      } {
        val newBoxActor = context.actorOf(Props(new BoxActor(BoxAppKeyPair(client_id, client_secret), accessTokens, streamPos)))
        BoxActor.send(Some(newBoxActor))
      }
    }
  }
  
  override def preStart {
    super.preStart
    self ! UpdateProjects
    FilemakerConnector.startAggregating(context)
    startBoxActor
  }

  override def receive = {
    case UpdateBoxAccessTokens(tokens: BoxAccessTokens) => DBProxy.setBoxToken(tokens)
    case UpdateProjects => updateProjects
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