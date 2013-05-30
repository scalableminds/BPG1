package models

import play.api.libs.json.Json

case class TagType(name: String)

object TagTypeDAO extends SecuredMongoJsonDAO[TagType]{
  val collectionName = "tagTypes"
    
  implicit val tagTypeFormat = Json.format[TagType]
}