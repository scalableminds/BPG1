package controllers

import securesocial.core.SecureSocial
import models.MongoDAO
import play.api.libs.json.Json
import play.api.libs.json.Format
import projectZoom.util.PlayActorSystem
import models.DAO
import play.api.libs.json.JsObject
import play.api.libs.json.OFormat
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.concurrent.Execution.Implicits._

trait JsonCRUDController extends CRUDController[JsObject] {
  implicit def formatter = Format.apply[JsObject](Reads.JsObjectReads, Writes.JsValueWrites)
}

trait CRUDController[T] extends SecureSocial {

  def dao: DAO[T]
  implicit def formatter: Format[T]

  def list(offset: Int, limit: Int) = SecuredAction { implicit request =>
    //TODO: restrict access
    Async {
      dao.findSome(offset, limit).map { l =>
        Ok(Json.toJson(l))
      }
    }
  }

  def read(id: String) = SecuredAction { implicit request =>
    //TODO: restrict access
    Async {
      dao.findById(id).map { l =>
        Ok(Json.toJson(l))
      }
    }
  }
}