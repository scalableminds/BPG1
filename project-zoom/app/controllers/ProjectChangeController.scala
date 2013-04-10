package controllers

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
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Input
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsArray

case class Join(username: String)
case class Quit(username: String)
case class Connected(enumerator: Enumerator[JsValue])
case class CannotConnect(msg: String)

object ProjectRoom {
  implicit val sys = Akka.system(Play.current)
  implicit val timeout = Timeout(1 second)

  val projectRooms = Agent[Map[String, ActorRef]](Map())

  def createRoom(_project: String) =
    sys.actorOf(Props(new ProjectRoom(_project)))

  def join(username: String, _project: String): scala.concurrent.Future[(Iteratee[JsValue, _], Enumerator[JsValue])] = {
    val room = projectRooms().get(_project).getOrElse {
      val r = createRoom(_project)
      projectRooms.send(_ + (_project -> r))
      r
    }
    (room ? Join(username)).map {

      case Connected(enumerator) =>

        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          Logger.debug("Client sent Message: " + event)
        }.mapDone { _ =>
          room ! Quit(username)
        }

        (iteratee, enumerator)

      case CannotConnect(error) =>

        // Connection error

        // A finished Iteratee sending EOF
        val iteratee = Done[JsValue, Unit]((), Input.EOF)

        // Send an error and close the socket
        val enumerator = Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))

        (iteratee, enumerator)

    }

  }
}

class ProjectRoom(_project: String) extends Actor {
  
  var members = Set.empty[String]
  val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]

  def receive = {
    
    case Join(username) => {
      if(members.contains(username)) {
        sender ! CannotConnect("This user already joined.")
      } else {
        Logger.debug(s"ProjectRoom.Join $username has joined the room")
        members = members + username
        sender ! Connected(chatEnumerator)
      }
    }

    case Quit(username) => {
      members = members - username
      Logger.debug(s"ProjectRoom.Quit $username has left the room")
    }
  }
  
  def notifyAll(kind: String, user: String, text: String) {
    val msg = JsObject(
      Seq(
        "kind" -> JsString(kind),
        "user" -> JsString(user),
        "message" -> JsString(text)
      )
    )
    chatChannel.push(msg)
  }
}

object ProjectChangeController extends ControllerBase {

  def joinChannel(_project: String) = WebSocket.async[JsValue] { request =>
     ProjectRoom.join("username", _project)
  }
}