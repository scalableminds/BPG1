package projectZoom.connector.Filemaker

import projectZoom.connector._
import projectZoom.util.PlayActorSystem
import projectZoom.util.SSH
import akka.actor._
import play.api.libs.concurrent.Akka
import play.api.Logger


class FilemakerConnector extends Connector with PlayActorSystem{
  val rHost = "172.16.23.54"
  val fmPort = 2399
  val SSHUserName = "fmpro"
  val SSHPassword = "toor"
    
  def startAggregating(implicit app: play.api.Application) = {
        SSH.createTunnel(rHost, 22, SSHUserName, SSHPassword, fmPort, "127.0.0.1", fmPort)
        val actor = Akka.system(app).actorOf(Props(new FilemakerActor(FilemakerAPI.create(
            FileMakerDBInfo("127.0.0.1", "dschoolDB.fmp12", "admin", "admin")))))
        actor ! StartAggregating
  }
  
  
}