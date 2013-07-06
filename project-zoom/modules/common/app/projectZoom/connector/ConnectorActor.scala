package projectZoom.connector

import akka.actor._
import projectZoom.core.artifact.ArtifactUpdate
import play.api.Logger
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import projectZoom.util.PlayConfig

case object StartAggregating
case class StartedAggregating(actor: ActorRef)
case object Aggregate
case object StopAggregating
case class StoppedAggregating(actor: ActorRef)

trait ConnectorInterface {
  def start()
  def aggregate()
  def stop()
}

trait ConnectorActor extends Actor with ConnectorInterface with PlayConfig {

  import context.become

  val TICKER_INTERVAL: FiniteDuration = 1 minute

  val updateTicker = context.system.scheduler.schedule(0 seconds, TICKER_INTERVAL, self, Aggregate)

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    updateTicker.cancel
    super.preRestart(reason, message)
  }

  def stopped: Receive = {
    case StartAggregating =>
      start()
      context.parent ! StartedAggregating(context.self)
      become(started)
  }

  def started: Receive = {
    case StopAggregating =>
      stop()
      context.parent ! StoppedAggregating(context.self)
      become(stopped)
    case Aggregate => aggregate()
  }

  def receive = stopped
}