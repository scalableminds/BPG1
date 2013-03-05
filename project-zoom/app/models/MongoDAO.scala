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
import projectZoom.bson.Bson
import projectZoom.util.MongoHelpers
import play.api.libs.json.JsObject

trait BSONDocumentHandler[T] extends BSONDocumentReader[T] with BSONDocumentWriter[T]

trait MongoJson extends MongoDAO[JsObject] {
  implicit object handler extends BSONDocumentHandler[JsObject] {
    def read(doc: BSONDocument)= Implicits.JsObjectReader.read(doc)
    def write(js: JsObject)= Implicits.JsObjectWriter.write(js)
  }
}

trait MongoDAO[T] extends MongoJSONHelpers{

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  def db: DefaultDB = ReactiveMongoPlugin.db
  def collection: reactivemongo.api.collections.default.BSONCollection

  implicit val handler: BSONDocumentHandler[T]

  def findHeadOption(attribute: String, value: String) = {
    collection.find(Bson.obj(attribute -> value)).one
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