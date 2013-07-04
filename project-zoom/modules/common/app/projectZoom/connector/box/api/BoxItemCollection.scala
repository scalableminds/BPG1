package projectZoom.connector.box.api

import play.api.libs.json._

case class BoxItemCollection(total_count: Int, entries: List[BoxMiniSource])

object BoxItemCollection extends Function2[Int, List[BoxMiniSource], BoxItemCollection]{
  implicit val BoxItemCollectionFormat: Reads[BoxItemCollection] = Json.reads[BoxItemCollection]
}