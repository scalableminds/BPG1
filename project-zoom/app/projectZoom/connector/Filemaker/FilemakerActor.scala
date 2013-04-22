package projectZoom.connector.Filemaker

import projectZoom.connector._
import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger

class FilemakerActor(filemaker: FilemakerAPI) extends KnowledgeAggregatorActor{
  val TICKER_INTERVAL = 1 minute

  var updateTicker: Cancellable = null
  
  def aggregate() = {
    publishProfiles(filemaker.extractStudents)
    publishProjects(filemaker.extractProjects)
  }

  def start = {
    Logger.debug("Starting update ticker")
    updateTicker = context.system.scheduler.schedule(0 seconds, TICKER_INTERVAL, self, Aggregate)
  }

  def stop = {
    updateTicker.cancel
  }
  
  override def preStart(){
    self ! StartAggregating
  }
}