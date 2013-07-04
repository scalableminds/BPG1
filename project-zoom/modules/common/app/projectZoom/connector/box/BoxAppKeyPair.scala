package projectZoom.connector.box

import projectZoom.util.PlayConfig

case class BoxAppKeyPair(client_id: String, client_secret: String)

object BoxAppKeyPair extends PlayConfig {
  def readFromConfig = {
    for{client_id <- config.getString("box.client_id")
        client_secret <- config.getString("box.client_secret")
     } yield BoxAppKeyPair(client_id, client_secret)
  }
}