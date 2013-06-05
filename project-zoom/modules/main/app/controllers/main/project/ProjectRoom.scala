package controllers.main.project

import akka.agent.Agent
import akka.actor.ActorRef
import play.api.libs.concurrent.Akka
import play.api.Play
import akka.actor.Props
import akka.actor.Actor
import play.api.mvc.WebSocket
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Iteratee
import akka.util.Timeout
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Input
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.iteratee.Concurrent
import securesocial.core.SecureSocial
import scala.concurrent.Future
import securesocial.core.UserId
import akka.actor.actorRef2Scala
import akka.pattern.ask
import controllers.common.ControllerBase
import projectZoom.core.event.EventSubscriber
import projectZoom.core.artifact._
import projectZoom.core.event.Event
import models.ProjectDAO
import models.ArtifactDAO
import models.GlobalDBAccess
import play.api.libs.json.JsArray
import play.api.libs.json.Json
import models.ArtifactLikeTransformers
import models.Project
import models.ArtifactLike
import akka.actor.PoisonPill
import projectZoom.util.StartableActor
import controllers.main.GraphUpdated

case class Join(userId: UserId)
case class Quit(userId: UserId)
case class Connected(enumerator: Enumerator[JsValue])
case class CannotConnect(msg: String)

trait ClosedChannelHelper {
  def closedChannel(error: String) = {
    // Connection error

    // A finished Iteratee sending EOF
    val iteratee = Done[JsValue, Unit]((), Input.EOF)

    // Send an error and close the socket
    val enumerator = Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))

    (iteratee, enumerator)
  }
}

object ProjectRoom extends ClosedChannelHelper {
  implicit val sys = Akka.system(Play.current)
  implicit val timeout = Timeout(1 second)

  val roomPlan = Agent[Map[String, ActorRef]](Map())

  def createRoom(_project: String) =
    sys.actorOf(Props(new ProjectRoom(_project)))

  def getOrCreateRoom(_project: String) =
    roomPlan().get(_project).getOrElse {
      val r = createRoom(_project)
      roomPlan.send(_ + (_project -> r))
      r
    }

  def join(userId: UserId, _project: String): scala.concurrent.Future[(Iteratee[JsValue, _], Enumerator[JsValue])] = {
    val room = getOrCreateRoom(_project)
    (room ? Join(userId)).map {

      case Connected(enumerator) =>

        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          Logger.debug("Client sent Message: " + event)
        }.mapDone { _ =>
          room ! Quit(userId)
        }

        (iteratee, enumerator)

      case CannotConnect(error) =>

        closedChannel(error)
    }

  }
}

class ProjectRoomEventDispatcher1 extends Actor with EventSubscriber with GlobalDBAccess {

  def forwardToProject(_project: String, e: ProjectUpdate) =
    ProjectRoom.roomPlan().get(_project).map { projectActor =>
      projectActor.forward(e)
    }

  def withProject[T](projectName: String)(f: Project => T) = {
    ProjectDAO.findProject(projectName).map(_.map { project =>
      f(project)
    })
  }

  def forwardProjectUpdate(projectName: String, update: ProjectUpdate) = {
    withProject(projectName) { project =>
      forwardToProject(project._id.stringify, update)
    }
  }

  def handleArtifactUpdate(artifact: ArtifactLike) = {
    forwardProjectUpdate(
      artifact.projectName,
      ProjectUpdate("artifacts", "update", ArtifactDAO.artifactLikeWrites.writes(artifact)))
  }

  def handleArtifactInserted(artifact: ArtifactLike) = {
    forwardProjectUpdate(
      artifact.projectName,
      ProjectUpdate("artifacts", "insert", ArtifactDAO.artifactLikeWrites.writes(artifact)))
  }

  def receive = {
    case e: ResourceInserted =>
      handleArtifactUpdate(e.artifact)

    case e: ResourceUpdated =>
      handleArtifactUpdate(e.artifact)

    case e: ArtifactInserted =>
      handleArtifactInserted(e.artifact)

    case e: ArtifactUpdated =>
      handleArtifactUpdate(e.artifact)

    case e: GraphUpdated =>
      println("reached! project: " + e.graph._project.toString)
      forwardToProject(
        e.graph._project.stringify,
        ProjectUpdate("graphs", "patch", e.patch, Some(e.graph._id.stringify)))
  }
}

object ProjectRoomEventDispatcher1 extends StartableActor[ProjectRoomEventDispatcher] {
  val name = "projectRoomEventDispatcher"
}

case class ProjectUpdate(collection: String, operation: String, value: JsValue, identifier: Option[String] = None)

class ProjectRoom(_project: String) extends Actor {

  implicit val projectUpdateWrites = Json.writes[ProjectUpdate]

  var members = Set.empty[UserId]
  val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]

  def receive = {

    case update: ProjectUpdate =>
      notifyAll(update)

    case Join(userId) => {
      if (members.contains(userId)) {
        sender ! CannotConnect("This user already joined.")
      } else {
        Logger.debug(s"ProjectRoom.Join ${userId.id} has joined the room")
        members = members + userId
        sender ! Connected(chatEnumerator)
      }
    }

    case Quit(userId) => {
      members = members - userId
      Logger.debug(s"ProjectRoom ${_project}. Quit ${userId.id} has left the room")
      if (members.isEmpty) {
        Logger.debug(s"ProjectRoom ${_project} is Empty. Shutdown! ")
        self ! PoisonPill
      }
    }
  }

  def notifyAll(update: ProjectUpdate) {
    val msg = Json.toJson(update)
    chatChannel.push(msg)
  }
}