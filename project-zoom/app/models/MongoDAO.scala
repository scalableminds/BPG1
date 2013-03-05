package models

import play.api.Play.current
import play.modules.reactivemongo._
import scala.concurrent.ExecutionContext
import play.api.libs.json.JsValue
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONObjectID

trait BSONDocumentHandler[T] extends BSONDocumentReader[T] with BSONDocumentWriter[T]

trait MongoDAO[T] {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  def db: DefaultDB = ReactiveMongoPlugin.db
  def collection: reactivemongo.api.collections.default.BSONCollection

  implicit val handler: BSONDocumentHandler[T]

  def findHeadOption(attribute: String, value: String) = {
    collection.find(BSONDocument(attribute -> BSONString(value))).one
  }

  def findById(id: String) = {
    collection.find(BSONDocument("_id" -> new BSONObjectID(id))).one
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