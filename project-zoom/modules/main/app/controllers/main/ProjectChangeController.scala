package controllers.main

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

  val projectRooms = Agent[Map[String, ActorRef]](Map())

  def createRoom(_project: String) =
    sys.actorOf(Props(new ProjectRoom(_project)))

  def join(userId: UserId, _project: String): scala.concurrent.Future[(Iteratee[JsValue, _], Enumerator[JsValue])] = {
    val room = projectRooms().get(_project).getOrElse {
      val r = createRoom(_project)
      projectRooms.send(_ + (_project -> r))
      r
    }
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

class ProjectRoom(_project: String) extends Actor {

  var members = Set.empty[UserId]
  val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]

  def receive = {

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
      Logger.debug(s"ProjectRoom.Quit {userId.id} has left the room")
    }
  }

  def notifyAll(kind: String, user: String, text: String) {
    val msg = JsObject(
      Seq(
        "kind" -> JsString(kind),
        "user" -> JsString(user),
        "message" -> JsString(text)))
    chatChannel.push(msg)
  }
}

object ProjectChangeController extends ControllerBase with SecureSocial with ClosedChannelHelper {

  def joinChannel(_project: String) = WebSocket.async[JsValue] { implicit request =>
    SecureSocial.currentUser(request).map { user =>
      ProjectRoom.join(user.id, _project)
    } getOrElse {
      Future.successful(closedChannel("Not logged in."))
    }
  }
}