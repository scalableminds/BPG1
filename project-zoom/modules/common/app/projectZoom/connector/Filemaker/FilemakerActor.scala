package projectZoom.connector.Filemaker

import projectZoom.connector._
import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import scala.concurrent.Future
import projectZoom.util.SSH

class FilemakerActor(filemaker: FilemakerAPI) extends KnowledgeAggregatorActor {

  val TICKER_INTERVAL = 1 minute

  def aggregate() = {
    publishProfiles(filemaker.extractStudents)
    val projects = filemaker.extractProjects
    publishProjects(projects)
  }

  def start = {
    Logger.debug("Starting FilemakerActor")
  }

  def stop = {
    Logger.debug("Stopping FilemakerActor")
  }
}