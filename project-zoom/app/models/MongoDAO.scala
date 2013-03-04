package models

import play.api.Play.current
import play.modules.reactivemongo._
import reactivemongo.bson._
import scala.concurrent.ExecutionContext
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers._

trait MongoDAO[T] {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  def db = ReactiveMongoPlugin.db
  def collection: reactivemongo.api.Collection

  implicit val reader: BSONReader[T]
  implicit val writer: BSONWriter[T]

  def findHeadOption(attribute: String, value: String) = {
    collection.find(BSONDocument(attribute -> BSONString(value))).headOption
  }

  def findById(id: String) = {
    collection.find(BSONDocument("_id" -> new BSONObjectID(id))).headOption
  }
  
  def insert(t: T) = {
    collection.insert(t)
  }
  
  def removeById(id: String) = {
    collection.remove(BSONDocument("_id" -> new BSONObjectID(id)))
  }
  
  def removeAll() = {
    collection.remove(BSONDocument())
  }
}