package projectZoom

import play.api.libs.json.JsValue
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import _root_.models.MongoJson
import _root_.models.User

object Graph extends MongoJson {
  def collection = db("graphs")

  val nodeType: Reads[User] = User.userFormat

  val node = (
    (__ \ 'id).read[Int] and
    (__ \ 'position).read[Int] and
    (__ \ 'payload).read(nodeType)).tupled

  val edge = (
    (__ \ 'from).read[Int] and
    (__ \ 'to).read[Int] and
    (__ \ 'comment).read[String]).tupled

  val cluster = (
    (__ \ 'id).read[Int] and
    (__ \ 'positions).read[Int] and
    (__ \ 'tags).read(list[String])).tupled

  val reads = (
    (__ \ 'id).read[String] and
    (__ \ 'group).read[Int] and
    (__ \ 'version).read[Int] and
    (__ \ 'nodes).read(list(node)) and
    (__ \ 'edges).read(list(node)) and
    (__ \ 'cluster).read(list(cluster))).tupled

  val reducePayloadToId =
    (__ \ 'payload).json.update((__ \ 'id).json.pick)

  val removePayloadDetails = (
    (__ \ 'nodes).json.update(reducePayloadToId))
}