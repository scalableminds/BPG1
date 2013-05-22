package projectZoom.connector.box

import play.api.libs.json._
import play.api.libs.ws._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger

case class BoxAppKeyPair(client_id: String, client_secret: String)

class BoxAPI(appKeys: BoxAppKeyPair) {

}