package models

import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._
import play.api.libs.json.JsValue
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

case class PermanentValue(key: String, value: JsValue)

object PermanentValueDAO extends UnsecuredMongoJsonDAO[PermanentValue] {
  val collectionName = "permanentValues"

  implicit val permanentValueFormat = Json.format[PermanentValue]
  
  def update(p: PermanentValue) = {
    collectionUpdate(Json.obj("key" -> p.key), permanentValueFormat.writes(p), true)
  }
}

object PermanentValueService extends GlobalDBAccess{
  import PermanentValueDAO.permanentValueFormat
  
  def get(key: String): Future[Option[JsValue]] = {
    PermanentValueDAO.find("key", key).one[PermanentValue].map(_.map(_.value))
  }
  
  def put(key: String, value: JsValue): JsValue = {
    PermanentValueDAO.update(PermanentValue(key, value))
    value
  }
  
  def getOrPut(key: String)(value: => JsValue) = {
    get(key).map( _.getOrElse {
      put(key, value)
      value
    })
  }
}