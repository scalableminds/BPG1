package models

import play.api.Play.current
import play.modules.reactivemongo._
import play.api.libs.json.JsValue
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONObjectID
import projectZoom.core.bson.Bson
import projectZoom.util.MongoHelpers
import play.api.libs.json.JsObject
import play.api.libs.json.Writes
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import reactivemongo.core.commands.LastError
import play.api.Logger

trait BSONDocumentHandler[T] extends BSONDocumentReader[T] with BSONDocumentWriter[T]

trait MongoJsonDAO extends MongoDAO[JsObject] {
  def insert[T](t: T)(implicit writer: Writes[T]): Future[LastError] = {
    writer.writes(t) match {
      case j: JsObject =>
        collection.insert(j)
      case _ =>
        val errorMsg = "Couldn't insert object because serializer didn't produce a JsObject."
        Logger.error(errorMsg)
        Future.successful(errorFromMsg(errorMsg))
    }
  }

  implicit object handler extends BSONDocumentHandler[JsObject] {
    def read(doc: BSONDocument) = Implicits.JsObjectReader.read(doc)
    def write(js: JsObject) = Implicits.JsObjectWriter.write(js)
  }
}

trait MongoDAO[T] extends MongoJSONHelpers {

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  def db: DefaultDB = ReactiveMongoPlugin.db
  def collection: reactivemongo.api.collections.default.BSONCollection

  implicit val handler: BSONDocumentHandler[T]

  def errorFromMsg(msg: String) = {
    LastError(ok = false, None, None, Some(msg), None, 0, false)
  }

  def findHeadOption(attribute: String, value: String) = {
    collection.find(Bson.obj(attribute -> value)).one
  }

  def findAll = {
    collection.find(Bson.obj()).cursor.toList
  }

  def findById(id: String) = {
    collection.find(BSONDocument("_id" -> new BSONObjectID(id))).one
  }

  def insert(t: T): Future[LastError] = {
    collection.insert(t)
  }

  def removeById(id: String) = {
    collection.remove(BSONDocument("_id" -> new BSONObjectID(id)))
  }

  def removeAll() = {
    collection.remove(BSONDocument())
  }
}