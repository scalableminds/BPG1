package projectZoom.connector

import akka.actor._
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._
import projectZoom.core.event.EventSubscriber
import projectZoom.connector.Filemaker._
import projectZoom.util.SSH
import projectZoom.util.{PlayActorSystem, StartableActor}
import play.api.Logger
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class CreatingTunnelFailed extends RuntimeException

class SupervisorActor extends EventSubscriber with PlayActorSystem{

  val rHost = "172.16.23.54"
  val fmPort = 2399
  val SSHUserName = "fmpro"
  val SSHPassword = "toor"
  Future{
    if (! SSH.createTunnel(rHost, 22, SSHUserName, SSHPassword, fmPort, "127.0.0.1", fmPort)){
        Logger.error("Could not create tunnel! No filemaker data will be available!")
    }
    else{
      Logger.info("created ssh tunnel - starting Filemaker Actor")
      context.actorOf(Props(new FilemakerActor(FilemakerAPI.create(FileMakerDBInfo("127.0.0.1", "dschoolDB.fmp12", "admin", "admin")))))
    }
  }

  override def receive = {
    case _ => println("TODO")
  }

  val connectors = List[ActorRef]()

  def updateProjects = {

  }

  def updateUsers = {

  }

  def updateSettings = {

  }

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 minute) {
    case _ => Restart
  }
}

object SupervisorActor extends StartableActor[SupervisorActor]
{
  def name = "supervisorActor"
}