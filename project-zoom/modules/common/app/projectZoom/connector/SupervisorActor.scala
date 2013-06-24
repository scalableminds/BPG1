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

case class BoxUpdated(accessTokens: BoxAccessTokens) extends Event

class SupervisorActor extends EventSubscriber with PlayActorSystem with PlayConfig {

  val projectCache = context.actorOf(ProjectCache("ProjectCache"))
  val fileProjectMatcher = context.actorOf(FileProjectMatcher("FileProjectMatcher"))

  val BoxActor = context.actorOf(Props[BoxActor], "BoxActor")
  val FilemakerActor = context.actorOf(Props[FilemakerActor], "FilemakerActor")
  
  //val test = context.actorOf(TestActor.props)
  
  FilemakerActor ! StartAggregating
  BoxActor ! StartAggregating    
  

  override def receive = {
    case BoxUpdated(accessTokens) => 
      BoxActor ! StopAggregating
      BoxActor ! BoxUpdated(accessTokens)
      BoxActor ! StartAggregating
  }

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 minute) {
    case _ => Restart
  }
  
}

object SupervisorActor extends StartableActor[SupervisorActor] {
  def name = "supervisorActor"
}