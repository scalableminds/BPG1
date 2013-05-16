package models

import play.api.libs.json.Json
import play.api.libs.json.JsObject
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Format

case class ResourceInfo(fileName: String, typ: String)

case class Resource(path: String, fileName: String, hash: String)

trait DefaultResourceTypes {
  val DEFAULT_TYP = "default"
}
object DefaultResourceTypes extends DefaultResourceTypes

trait ResourceHelpers {
  implicit val resourceFormat: Format[Resource] = Json.format[Resource]

  implicit val resourceInfoFormat: Format[ResourceInfo] = Json.format[ResourceInfo]

  def resourceCreateFrom(ri: ResourceInfo, path: String, hash: String) =
    Resource(
      path,
      ri.fileName,
      hash)
}