package models

import play.api.Play.current
import play.modules.reactivemongo._
import reactivemongo.bson._
import scala.concurrent.ExecutionContext
import reactivemongo.bson.handlers.BSONReader
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter

trait MongoDAO[T] {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  def db = ReactiveMongoPlugin.db
  def collection: reactivemongo.api.Collection

  implicit val reader: BSONReader[T]

  def findHeadOption(attribute: String, value: String) = {
    collection.find(BSONDocument(attribute -> BSONString(value))).headOption
  }

  def findById(id: String) = {
    collection.find(BSONDocument("_id" -> new BSONObjectID(id))).headOption
  }

}