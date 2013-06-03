package models

import reactivemongo.bson.BSONObjectID
import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats.BSONObjectIDFormat
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import scala.math

case class Color(r: Int, b: Int, g: Int)

object Color extends Function3[Int, Int, Int, Color] {
  implicit val colorFormat = Json.format[Color]

  private def random255 = (math.random * 256).toInt

  def random = Color(random255, random255, random255)
}

case class Tag(name: String, color: Color, _tagType: Option[BSONObjectID])

object TagDAO extends SecuredMongoJsonDAO[Tag] {
  val collectionName = "tags"

  val manager = collection.indexesManager
  manager.ensure(Index(List("name" -> Ascending), unique = true))

  implicit val tagFormat = Json.format[Tag]

  def ensureTag(tagName: String)(implicit ctx: DBAccessContext) = {
    collectionUpdate(Json.obj("name" -> tagName), Json.obj( "$setOnInsert" -> Json.obj(
      "name" -> tagName,
      "color" -> Color.random)), upsert = true)
  }

  def findByName(name: String)(implicit ctx: DBAccessContext) = {
    find("name", name).one[JsObject]
  }
}