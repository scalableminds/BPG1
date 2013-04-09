package models

import play.api.libs.json.Json
import play.api.libs.json.JsObject

case class ResourceInfo(fileName: String, typ: String)

case class Resource(path: String, fileName: String, typ: String, hash: String, updatedAt: Long = System.currentTimeMillis)

trait DefaultResourceTypes{
  val DEFAULT_TYP = "default"
}

trait ResourceFactory {
  def from(ri: ResourceInfo, path: String, hash: String) =
    Resource(
      path,
      ri.fileName,
      ri.typ,
      hash)
}

object Resource extends MongoJson with ResourceFactory with DefaultResourceTypes{
  override def collection = db("resources")

  implicit val resourceWriter = Json.writes[Resource]

  def findByResourceQ(resource: Resource): JsObject =
    findByResourceQ(resource.path, resource.fileName, resource.typ)

  def findByResourceQ(path: String, fileName: String, typ: String) =
    Json.obj("path" -> path, "fileName" -> fileName, "typ" -> typ)

  def update(resoure: Resource) = {
    collection.update(findByResourceQ(resoure),
      Json.obj("$set" -> resoure), upsert = true)
  }
}