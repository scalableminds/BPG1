package models

import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.JsObject
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Writes._

trait ResourceLike {
  def name: String
  def typ: String
  def hash: Option[String]

  def withHash(h: String): ResourceLike

  def isSameAs(o: ResourceLike) =
    name == o.name && typ == o.typ
}

trait ResourceLikeTransformers {
  def toTuple(r: ResourceLike) =
    (r.name, r.typ, r.hash)

  implicit val resourceLikeWrites: Writes[ResourceLike] =
    ((__ \ 'name).write[String] and
      (__ \ 'typ).write[String] and
      (__ \ 'hash).write[Option[String]])(toTuple _)
}

case class Resource(name: String, typ: String, hash: Option[String] = None)
    extends ResourceLike {

  def withHash(h: String) = this.copy(hash = Some(h))
}

trait ResourceTypes {
  val DEFAULT_TYP = "default"
  val PRIMARY_THUMBNAIL = "primaryThumbnail"
  val SECONDARY_THUMBNAIL = "secondaryThumbnail"
}
object DefaultResourceTypes extends ResourceTypes

trait ResourceHelpers extends ResourceLikeTransformers{
  implicit val resourceFormat: Format[Resource] = Json.format[Resource]
}