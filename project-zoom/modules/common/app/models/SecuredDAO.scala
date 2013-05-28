package models

import scala.concurrent.Future
import reactivemongo.core.commands.LastError
import play.api.libs.json.JsObject
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import play.api.Logger

trait SecuredMongoJsonDAO[T] extends MongoJsonDAO[T] with SecuredJsonDAO[T]

trait UnsecuredMongoJsonDAO[T] extends MongoJsonDAO[T] with UnsecuredJsonDAO[T]

trait UnsecuredMongoDAO[T] extends MongoDAO[T] with UnsecuredDAO[T]

trait SecuredJsonDAO[T] extends SecuredDAO[JsObject] { this: MongoJsonDAO[T] =>

}

trait UnsecuredJsonDAO[T] extends UnsecuredDAO[JsObject] { this: MongoJsonDAO[T] =>

}

trait UnsecuredDAO[T] extends SecuredDAO[T] { this: MongoDAO[T] =>
  implicit val ctx = UnAuthedAccessContext

  override def isAllowedToInsert(implicit ctx: DBAccessContext) = true

  override def removeQueryFilter(implicit ctx: DBAccessContext) = Json.obj()

  override def updateQueryFilter(implicit ctx: DBAccessContext) = Json.obj()

  override def findQueryFilter(implicit ctx: DBAccessContext) = Json.obj()
}

trait GlobalDBAccess {
  implicit val ctx: DBAccessContext = GlobalAccessContext
}

trait AllowEverytingDBAccessValidator extends DBAccessValidator {
  def isAllowedToInsert(implicit ctx: DBAccessContext): Boolean = true

  def removeQueryFilter(implicit ctx: DBAccessContext): JsObject = Json.obj()

  def updateQueryFilter(implicit ctx: DBAccessContext): JsObject = Json.obj()

  def findQueryFilter(implicit ctx: DBAccessContext): JsObject = Json.obj()
}

trait AllowEyerthingDBAccessFactory extends DBAccessFactory {
  def createACL(toInsert: JsObject)(implicit ctx: DBAccessContext): JsObject = Json.obj()
}

trait DBAccessFactory {
  def createACL(toInsert: JsObject)(implicit ctx: DBAccessContext): JsObject
}

trait DBAccessValidator {
  def isAllowedToInsert(implicit ctx: DBAccessContext): Boolean

  def removeQueryFilter(implicit ctx: DBAccessContext): JsObject

  def updateQueryFilter(implicit ctx: DBAccessContext): JsObject

  def findQueryFilter(implicit ctx: DBAccessContext): JsObject
}

trait SecuredDAO[T] extends DBAccessValidator with DBAccessFactory with AllowEyerthingDBAccessFactory with AllowEverytingDBAccessValidator { this: MongoDAO[T] =>

  def withValidId(sid: String)(f: BSONObjectID => Future[LastError]) =
    BSONObjectID
      .parse(sid)
      .map(f)
      .getOrElse(Future.successful(
        LastError(false, None, None, Some("Couldn't parse ObjectId"), None, 0, false)))

  def collectionInsert(js: JsObject)(implicit ctx: DBAccessContext): Future[LastError] = {
    if (ctx.globalAccess || isAllowedToInsert) {
      val future = collection.insert(js ++ createACL(js))
      future.onFailure {
        case e: Throwable =>
          Logger.error(e.toString)
      }
      future
    } else {
      Future.successful(
        LastError(false, None, None, Some("Access denied"), None, 0, false))
    }
  }

  def collectionFind(query: JsObject = Json.obj())(implicit ctx: DBAccessContext) = {
    if (ctx.globalAccess)
      collection.find(query)
    else
      collection.find(query ++ findQueryFilter)
  }

  def collectionUpdate(query: JsObject, update: JsObject, upsert: Boolean = false, multi: Boolean = false)(implicit ctx: DBAccessContext) = {
    val isUpsertAllowed = upsert && (ctx.globalAccess || isAllowedToInsert)
    val u =
      if (isUpsertAllowed)
        update ++ createACL(update)
      else
        update

    val q =
      if (ctx.globalAccess)
        query
      else
        query ++ updateQueryFilter

    collection.update(
      q,
      u,
      upsert = isUpsertAllowed,
      multi = multi)
  }

  def collectionRemove(js: JsObject)(implicit ctx: DBAccessContext) = {
    if (ctx.globalAccess)
      collection.remove(js)
    else
      collection.remove(js ++ removeQueryFilter)
  }
}