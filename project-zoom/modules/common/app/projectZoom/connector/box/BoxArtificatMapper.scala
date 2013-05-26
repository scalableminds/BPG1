package projectZoom.connector.box

import play.api.libs.json._
import models.ArtifactInfo
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.JsArray
import play.api.Logger

class BoxArtificatMapper(box: BoxAPI) {

  val infoFields = List("created_at", "created_by", "name")
  
}