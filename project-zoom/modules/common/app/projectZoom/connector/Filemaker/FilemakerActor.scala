package projectZoom.connector.Filemaker

import projectZoom.connector._
import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import scala.concurrent.Future
import projectZoom.util.SSH
import akka.agent._
import projectZoom.util.PlayActorSystem

class FilemakerActor extends KnowledgeAggregatorActor with PlayActorSystem {

  val filemaker = Agent[Option[FilemakerAPI]](None)

  def aggregate() = {
    filemaker().orElse {
      val newAPI = FilemakerAPI.create
      filemaker.send(newAPI)
      newAPI
    }.foreach { fm =>
      publishProfiles(fm.extractStudents)
      publishProjects(fm.extractProjects)
    }
  }

  def start = {
    Logger.debug("Starting FilemakerActor")
  }

  def stop = {
    Logger.debug("Stopping FilemakerActor")
    filemaker.send(None)
  }
}

