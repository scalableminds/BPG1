package models

import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._

case class Color(r: Int, b: Int, g: Int)

case class Tag(name: String, color: Color, _tagType: BSONObjectID)

object TagDAO extends SecuredMongoJsonDAO{
  val collectionName = "tags"

  implicit val colorFormat = Json.format[Color]
  
  implicit val tagFormat = Json.format[Tag]
}