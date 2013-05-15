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
import java.io.Writer

trait DAO[T] extends BaseDAO[T] {
  def findHeadOption(attribute: String, value: String)(implicit ctx: DBAccessContext): Future[Option[T]]

  def findSome(offset: Int, limit: Int)(implicit ctx: DBAccessContext): Future[List[T]]

  def findAll(implicit ctx: DBAccessContext): Future[List[T]]

  def findOneById(id: String)(implicit ctx: DBAccessContext): Future[Option[T]]

  def removeById(id: String)(implicit ctx: DBAccessContext): Future[LastError]

  def removeAll(implicit ctx: DBAccessContext): Future[LastError]
}

trait BaseDAO[T] {
  def collectionInsert(t: JsObject)(implicit ctx: DBAccessContext): Future[LastError]

  def collectionFind(query: JsObject)(implicit ctx: DBAccessContext): GenericQueryBuilder[JsObject, play.api.libs.json.Reads, play.api.libs.json.Writes]

  def collectionUpdate(query: JsObject, update: JsObject, upsert: Boolean = false, multi: Boolean = false)(implicit ctx: DBAccessContext): Future[LastError]

  def collectionRemove(js: JsObject)(implicit ctx: DBAccessContext): Future[LastError]
}

case class AuthedAccessContext(u: User) extends DBAccessContext {
  override def user = Some(u)
}

case object UnAuthedAccessContext extends DBAccessContext

case object GlobalAccessContext extends DBAccessContext {
  override val globalAccess = true
}

trait DBAccessContext {
  def user: Option[User] = None
  def globalAccess: Boolean = false
}

trait MongoJsonDAO extends MongoDAO[JsObject] {
  def insert[T](t: T)(implicit ctx: DBAccessContext, writer: OWrites[T]): Future[LastError] = {
    super.insert(writer.writes(t))
  }

  implicit object formatter extends OFormat[JsObject] {
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
  implicit def formatter: OFormat[T]

  def db: DefaultDB = ReactiveMongoPlugin.db
  lazy val collection = db.collection[JSONCollection](collectionName)

  def errorFromMsg(msg: String) = {
    LastError(ok = false, None, None, Some(msg), None, 0, false)
  }

  def findHeadOption(attribute: String, value: String)(implicit ctx: DBAccessContext) = {
    find(attribute, value).one[T]
  }
  
  def findOne(implicit ctx: DBAccessContext) = {
    collectionFind(Json.obj()).one[T]
  }

  def find(attribute: String, value: String)(implicit ctx: DBAccessContext): GenericQueryBuilder[JsObject, play.api.libs.json.Reads, play.api.libs.json.Writes] = {
    collectionFind(Json.obj(attribute -> value))
  }

  def remove(attribute: String, value: String)(implicit ctx: DBAccessContext): Future[LastError] = {
    collectionRemove(Json.obj(attribute -> value))
  }

  def findSome(offset: Int, limit: Int)(implicit ctx: DBAccessContext): Future[List[T]] = {
    takeSome(
      collectionFind(Json.obj()),
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

  def findAll(implicit ctx: DBAccessContext) = {
    collectionFind(Json.obj()).cursor[T].toList
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

  def findByEither(fields: (String, Function[String, Option[String]])*)(query: String)(implicit ctx: DBAccessContext) = {
    collectionFind(Json.obj(
      "$or" -> fields.flatMap {
        case (field, mapper) =>
          mapper(query).map(value => Json.obj(field -> value))
      }))
  }

  def findOneById(id: String)(implicit ctx: DBAccessContext) = {
    withId[Option[T]](id, errorValue = None) { bid =>
      collectionFind(Json.obj("_id" -> bid)).one[T]
    }
  }

  def removeById(id: String)(implicit ctx: DBAccessContext) = {
    withId(id, errorValue = LastError(false, None, None, Some(s"failed to parse objectId $id"), None, 0, false)) { bid =>
      collectionRemove(Json.obj("_id" -> new BSONObjectID(id)))
    }
  }

  def removeAll(implicit ctx: DBAccessContext) = {
    collectionRemove(Json.obj())
  }
  
  def insert(t:T)(implicit ctx: DBAccessContext): Future[LastError] = {
    collectionInsert(formatter.writes(t))
  }
}