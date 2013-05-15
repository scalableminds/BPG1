package projectZoom.core.settings

import projectZoom.util.StartableActor
import akka.actor.Actor
import javax.swing.text.html.HTML
import play.api.data.Form
import play.api.libs.json.JsObject
import akka.agent.Agent

case class SettingsPage(html: HTML, form: Form[JsObject])

case class RegisterSettingsPage(identifier: String, settingsPage: SettingsPage)
case class UnRegisterSettingsPage(identifier: String)
case class RequestSettingsPage(identifier: String)

class SettingsActor extends Actor {
  implicit val sys = context.system

  val settingsPages = Agent[Map[String, SettingsPage]](Map())
  def receive = {
    case RegisterSettingsPage(identifier, settingsPage) =>
      settingsPages.send(_ + (identifier -> settingsPage))
    case UnRegisterSettingsPage(identifier) =>
      settingsPages.send(_ - identifier)
    case RequestSettingsPage(identifier) =>
      sender ! settingsPages().get(identifier)
  }
}

object SettingsActor extends StartableActor[SettingsActor] {
  def name = "settingsActor"
}