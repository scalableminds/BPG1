package projectZoom.util

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.LocalPortForwarder;

import play.api.Logger

import akka.agent._

case class SSHTunnel(con: Connection, portForwarder: LocalPortForwarder, lPort: Int, rHost: String, rPort: Int)

object SSH extends PlayActorSystem {
  val sshTunnels = Agent(List[SSHTunnel]())
  
  def doesTunnelExist(lPort: Int, rHost: String, rPort: Int) = {
    sshTunnels().find(tun => 
      lPort == tun.lPort &&
      rHost == tun.rHost &&
      rPort == tun.rPort
    ).map{tun =>
      if (tun.con.isAuthenticationComplete())
        true
      else{
        tun.portForwarder.close
        tun.con.close()
        sshTunnels send (_.filterNot(
          _ == tun
        ))
        false
      }
    }.getOrElse(false)
  }

  def createTunnel(sshdHost: String, sshdPort: Int,
    userName: String, password: String,
    lPort: Int, rHost: String, rPort: Int) = {
    
    if(doesTunnelExist(lPort, rHost, lPort))
      true
    else{
      val conn = new Connection(sshdHost, sshdPort)
      conn.connect()
      if (!conn.authenticateWithPassword(userName, password)) {
        Logger.error("authentication with password failed")
        false
      } else {
        val localPortForwarder = conn.createLocalPortForwarder(lPort, rHost, rPort)
        sshTunnels send (SSHTunnel(conn, localPortForwarder, lPort, rHost, rPort) :: _)
        Logger.info(s"created ssh tunnel from $lPort to $rHost:$rPort")
        true
      }
    }
  }
}