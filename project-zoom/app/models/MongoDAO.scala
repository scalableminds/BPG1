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
import reactivemongo.api.collections.GenericQueryBuilder
import play.modules.reactivemongo.json.collection.JSONGenericHandlers
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.Success

trait DAO[T] {
  def findHeadOption(attribute: String, value: String): Future[Option[T]]

  def findSome(offset: Int, limit: Int): Future[List[T]]

  def findAll: Future[List[T]]

  def findOneById(id: String): Future[Option[T]]

  def insert(t: T): Future[LastError]

  def removeById(id: String): Future[LastError]

  def removeAll(): Future[LastError]
}

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

  implicit object formatter extends Format[JsObject] {
    def writes(js: JsObject) = js
    def reads(js: JsValue) = js match {
      case j: JsObject => JsSuccess(j)
      case _           => JsError()
    }
  }
}

trait MongoDAO[T] extends DAO[T] {
  import play.modules.reactivemongo.json.BSONFormats.BSONObjectIDFormat
  def collectionName: String
  implicit def formatter: Format[T]

  def db: DefaultDB = ReactiveMongoPlugin.db
  lazy val collection = db.collection[JSONCollection](collectionName)

  def errorFromMsg(msg: String) = {
    LastError(ok = false, None, None, Some(msg), None, 0, false)
  }

  def findHeadOption(attribute: String, value: String) = {
    find(attribute, value).one[T]
  }

  def find(attribute: String, value: String) = {
    collection.find(Json.obj(attribute -> value))
  }

  def remove(attribute: String, value: String) = {
    collection.remove(Json.obj(attribute -> value))
  }

  def findSome(offset: Int, limit: Int): Future[List[T]] = {
    takeSome(
      collection.find(Json.obj()),
      offset,
      limit)
  }

  def takeSome(q: GenericQueryBuilder[JsObject, Reads, Writes], offset: Int, limit: Int) = {
    val options = QueryOpts(skipN = offset, batchSizeN = limit)
    val document = Json.obj(
      "$oderby" -> Json.obj(
        "_id" -> 1))
    q
      .options(options)
      .sort(document)
      .cursor[T]
      .collect[List](limit)
  }

  def findAll = {
    collection.find(Json.obj()).cursor[T].toList
  }
  
  def toMongoObjectIdString(id: String) = 
    BSONObjectID.parse(id).map(oid => Json.toJson(oid).toString).toOption

  def withId[T](id: String, errorValue: => T)(f: BSONObjectID => Future[T]) = {
    BSONObjectID.parse(id) match {
      case Success(bid) =>
        f(bid)
      case _ =>
        Logger.error(s"Failed to parse objectId: $id")
        Future.successful(errorValue)
    }
  }
  
  
  def findByEither(fields: (String, Function[String, Option[String]])*)(query: String) = {
    collection.find(Json.obj(
      "$or" -> fields.flatMap {
        case (field, mapper) =>
          mapper(query).map(value => Json.obj(field -> value))
      }))
  }

  def findOneById(id: String) = {
    withId[Option[T]](id, errorValue = None) { bid =>
      collection.find(Json.obj("_id" -> bid)).one[T]
    }
  }

  def insert(t: T): Future[LastError] = {
    collection.insert(t)
  }

  def update(query: JsObject, t: T, upsert: Boolean, multi: Boolean) = {
    collection.update(query, t, upsert = upsert, multi = multi)
  }

  def removeById(id: String) = {
    withId(id, errorValue = LastError(false, None, None, Some(s"failed to parse objectId $id"), None, 0, false)) { bid =>
      collection.remove(Json.obj("_id" -> new BSONObjectID(id)))
    }
  }

  def removeAll() = {
    collection.remove(Json.obj())
  }
}