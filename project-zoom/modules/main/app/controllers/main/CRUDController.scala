package controllers.main

import securesocial.core.SecureSocial
import models.Implicits._
import play.api.libs.json.Json
import play.api.libs.json.Format
import models.DAO
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.concurrent.Execution.Implicits._
import projectZoom.util.MongoHelpers
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import models.DBAccessContext
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError

trait JsonCRUDController extends CRUDController[JsObject] {
  implicit def formatter = Format.apply[JsObject](Reads.JsObjectReads, Writes.JsValueWrites)
}

trait ListPortionHelpers {
  def withPortionInfo(js: JsValue, offset: Int, limit: Int) = {
    Json.obj(
      "offset" -> offset,
      "limit" -> limit,
      "content" -> js)
  }
}

trait CRUDController[T] extends SecureSocial with ListPortionHelpers with MongoHelpers {

  def dao: DAO[T]
  implicit def formatter: Format[T]

  def displayReader(implicit ctx: DBAccessContext): Reads[JsObject] = Reads.JsObjectReads

  def singleObjectFinder(id: String)(implicit ctx: DBAccessContext) =
    dao.findOneById(id)

  def createSingleResult(obj: T)(implicit ctx: DBAccessContext) = {
    Json.toJson(obj).transform(beautifyObjectId andThen displayReader) match {
      case JsSuccess(value, _) => value
      case e: JsError =>
        throw new Exception(s"Invalid DB content. Collection: ${dao.collectionName} Error: $e")
    }
  }

  def listItems(offset: Int, limit: Int)(resultTransformation: T => JsObject)(implicit ctx: DBAccessContext) = {
    dao.findSome(offset, limit).map { l =>
      Ok(withPortionInfo(
        Json.toJson(l.map(resultTransformation)), offset, limit))
    }
  }

  def list(offset: Int, limit: Int) = SecuredAction(ajaxCall = true) { implicit request =>
    Async {
      listItems(offset, limit)(createSingleResult)
    }
  }

  def read(id: String) = SecuredAction(ajaxCall = true) { implicit request =>
    Async {
      singleObjectFinder(id).map {
        case Some(obj) =>
          Ok(createSingleResult(obj))
        case _ =>
          Ok(Json.obj())
      }
    }
  }
}