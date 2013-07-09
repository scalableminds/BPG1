package models

import play.api.libs.json.Json

case class TagType(name: String)

object TagTypeDAO extends SecuredMongoJsonDAO[TagType]{
    /**
   * Name of the DB collection
   */
  val collectionName = "tagTypes"
    
  implicit val tagTypeFormat = Json.format[TagType]
}