package projectZoom.connector.incom

import akka.actor._
import projectZoom.util.PlayConfig
import play.api.libs.concurrent.Execution.Implicits._
import projectZoom.connector.ConnectorActor
import scala.concurrent.duration._
import play.api.Logger
import play.api.libs.ws._

class IncomActor(incom: IncomAPI) extends ConnectorActor with PlayConfig {

  val TICKER_INTERVAL = 1 minute

  var updateTicker: Cancellable = null
  
  def start = {
    Logger.debug("starting incom ticker")
    updateTicker = context.system.scheduler.schedule(0 seconds, TICKER_INTERVAL){
      Logger.info("incom tick")
    }
  }
  
  def stop = {
    updateTicker.cancel
  }
}