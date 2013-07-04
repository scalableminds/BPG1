package projectZoom.connector

import akka.agent._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import projectZoom.util.PlayActorSystem
import org.joda.time.DateTime
import models.ProjectLike
import play.Logger
import akka.actor._
import projectZoom.core.event._
import scala.util.{Success, Failure}

case object ProjectsUpdated
case object ProjectsRequest

class ProjectCache extends Actor with EventSubscriber {
  
  Logger.debug(s"ProjectCachePath: ${context.self.path.toString}")
  import context.become
  
  def upToDate(projects: List[ProjectLike]): Receive = {
    case ProjectsRequest => Logger.debug("ProjectCache up-to-date") 
        sender ! projects
    case ProjectsUpdated => become(outDated)
  }
  
  def outDated: Receive = {
    case ProjectsRequest => 
      Logger.debug("ProjectCache outdated, requesting new data")
      DBProxy.getProjects.onComplete{
        case Success(projects) => sender ! projects
            become(upToDate(projects))
        case Failure(err) => Logger.error(s"ProjectCache failed reading Projects from DB:\n$err")
      }
  }

  def receive = outDated
}

object ProjectCache {
  def apply(name: String): Props = Props(() => new ProjectCache, name)
}