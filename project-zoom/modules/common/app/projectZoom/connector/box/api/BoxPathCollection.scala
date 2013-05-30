package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class BoxPathCollection(total_count: Int, entries: List[BoxMiniFolder]) {
  val path = entries.tail.foldLeft("")((p, entry) => s"$p/${entry.name}")
}

object BoxPathCollection extends Function2[Int, List[BoxMiniFolder], BoxPathCollection] {
  implicit val BoxPathCollectionReads: Reads[BoxPathCollection] = Json.reads[BoxPathCollection]
}