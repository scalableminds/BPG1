package models

import play.api.libs.json.Json
import play.api.libs.json.JsObject
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Format

trait ResourceLike {
  def fileName: String
  def typ: String
}

case class ResourceInfo(fileName: String, typ: String)
  extends ResourceLike

case class Resource(fileName: String, hash: String, typ: String)
  extends ResourceLike
  
trait DefaultResourceTypes {
  val DEFAULT_TYP = "default"
}
object DefaultResourceTypes extends DefaultResourceTypes

trait ResourceHelpers {
  implicit val resourceFormat: Format[Resource] = Json.format[Resource]

  implicit val resourceInfoFormat: Format[ResourceInfo] = Json.format[ResourceInfo]

  def resourceCreateFrom(ri: ResourceInfo, hash: String) =
    Resource(
      ri.fileName,
      hash,
      ri.typ)
}