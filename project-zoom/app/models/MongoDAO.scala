package models

import play.api.Play.current
import play.modules.reactivemongo._
import reactivemongo.api.collections._
import reactivemongo.api._
import play.api.libs.json._
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONObjectID
import projectZoom.util.MongoHelpers
import play.api.libs.json.JsObject
import play.api.libs.json.Writes
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import reactivemongo.core.commands.LastError
import play.api.Logger
import reactivemongo.bson.buffer._
import play.modules.reactivemongo.json.collection.JSONGenericHandlers
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

trait MongoJsonDAO extends MongoDAO[JsObject] {
  def insert[T](t: T)(implicit writer: Writes[T]): Future[LastError] = {
    Logger.debug("INSERT: " + t)
    writer.writes(t) match {
      case j: JsObject =>
        collection.insert(j)
      case _ =>
        val errorMsg = "Couldn't insert object because serializer didn't produce a JsObject."
        Logger.error(errorMsg)
        Future.successful(errorFromMsg(errorMsg))
    }
  }

  implicit object formatter extends Format[JsObject] {
    def writes(js: JsObject) = js
    def reads(js: JsValue) = js match {
      case j: JsObject => JsSuccess(j)
      case _           => JsError()
    }
  }
}

trait MongoDAO[T] {
  import play.modules.reactivemongo.json.BSONFormats.BSONObjectIDFormat
  def collectionName: String
  implicit def formatter: Format[T]

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  def db: DefaultDB = ReactiveMongoPlugin.db
  lazy val collection = db.collection[JSONCollection](collectionName)

  def errorFromMsg(msg: String) = {
    LastError(ok = false, None, None, Some(msg), None, 0, false)
  }

  def findHeadOption(attribute: String, value: String) = {
    collection.find(Json.obj(attribute -> value)).one[T]
  }

  def findSome(offset: Int, limit: Int): Future[List[T]] = {
    val options = QueryOpts(skipN = offset, batchSizeN = limit)
    val document = Json.obj(
      "$oderby" -> Json.obj(
        "_id" -> 1))
    collection
      .find(Json.obj())
      .options(options)
      .sort(document)
      .cursor[T]
      .collect[List](limit)
  }

  def findAll = {
    collection.find(Json.obj()).cursor[T].toList
  }

  def findById(id: String) = {
    collection.find(Json.obj("_id" -> new BSONObjectID(id))).one[T]
  }

  def insert(t: T): Future[LastError] = {
    val e = collection.insert(t)(formatter,ec)
    e.map( r => println("Insertion result: " + r))
    e
  }

  def removeById(id: String) = {
    collection.remove(Json.obj("_id" -> new BSONObjectID(id)))
  }

  def removeAll() = {
    collection.remove(Json.obj())
  }
}