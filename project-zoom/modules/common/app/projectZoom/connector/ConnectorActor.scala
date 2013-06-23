package projectZoom.connector

import akka.actor._
import projectZoom.core.artifact.ArtifactUpdate
import play.api.Logger
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import projectZoom.util.PlayConfig

case object StartAggregating
case object Aggregate
case object StopAggregating

trait ConnectorInterface{
  def start()
  def aggregate()
  def stop()
}

trait ConnectorActor extends Actor with ConnectorInterface with PlayConfig{
  
  import context.become
  
  val TICKER_INTERVAL: FiniteDuration
  
  val updateTicker = context.system.scheduler.schedule(0 seconds, TICKER_INTERVAL, self, Aggregate)
  
  def stopped: Receive = {
    case StartAggregating =>
      start()
      become(started)
  }
  
  def started: Receive = {
    case StopAggregating =>
      stop()
      become(stopped)
    case Aggregate => aggregate()
  }
    
  def receive = stopped
}