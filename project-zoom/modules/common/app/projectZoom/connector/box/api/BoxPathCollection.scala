package projectZoom.connector.box.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class BoxPathCollection(total_count: Int, entries: List[BoxMiniFolder]) {
  val path = if(entries.isEmpty) List[String]() else entries.tail.map(folder => folder.name)
  val pathString = path.foldLeft("")((p, name) => s"$p/${name}")
  
  def relocate(index: Int, name: String) = this.copy(entries = entries.updated(index, entries(index).rename(name)))
}

object BoxPathCollection extends Function2[Int, List[BoxMiniFolder], BoxPathCollection] {
  implicit val BoxPathCollectionReads: Reads[BoxPathCollection] = Json.reads[BoxPathCollection]
}