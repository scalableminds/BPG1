package projectZoom.connector.Filemaker

import projectZoom.connector._
import projectZoom.util.PlayActorSystem
import projectZoom.util.SSH
import akka.actor._
import play.api.libs.concurrent.Akka
import play.api.Logger
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

object FilemakerConnector extends Connector with PlayActorSystem {
  val rHost = "172.16.23.54"
  val fmPort = 2399
  val SSHUserName = "fmpro"
  val SSHPassword = "toor"
  def startAggregating(context: ActorRefFactory) = {
    Future(SSH.createTunnel(rHost, 22, SSHUserName, SSHPassword, fmPort, "127.0.0.1", fmPort)).map { created =>
      if (created) {
        FilemakerAPI
          .create(FileMakerDBInfo("127.0.0.1", "dschoolDB.fmp12", "admin", "admin"))
          .map { api =>
            val actor = context.actorOf(Props(new FilemakerActor(api)))
            actor ! StartAggregating
          }
          .recover {
            case e: Exception =>
              Logger.error("Failed to initialize FileMakerApi due to: " + e)
          }
      } else {
        Logger.error("Failed to create ssh tunnel")
      }
    }
  }
}