package projectZoom.connector.dropbox

import akka.actor._
import projectZoom.util.PlayConfig
import play.api.libs.oauth.RequestToken
import play.api.libs.concurrent.Execution.Implicits._
import projectZoom.connector.ConnectorActor
import scala.concurrent.duration._
import play.api.Logger

case class DropboxAuth(
  user: String,
  password: String,
  appSignature: String,
  appKey: String)

class DropboxActor(dropbox: DropboxAPI) extends ConnectorActor with PlayConfig {

  def aggregate = {
    dropbox.updateLocalDropbox.map { l =>
      Logger.error("Got the list! L: " + l.size)
      l.map { artifactUpdate =>
        //artifactActor ! artifactUpdate
      }
    }
  }
  
  def start = Logger.debug("Starting Dropbox Actor")
  def stop = Logger.debug("Stopping Dropbox Actor")
}