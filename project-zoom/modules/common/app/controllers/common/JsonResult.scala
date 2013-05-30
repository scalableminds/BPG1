package controllers.common

import play.api.templates.Html
import play.api.libs.json.Json
import play.api.mvc.SimpleResult
import play.api.libs.json.JsObject
import play.api.mvc.Request
import net.liftweb.common.{ Box, Full, Empty, Failure, ParamFailure }
import play.api.mvc.Result
import play.api.mvc.Results
import play.api.mvc.ResponseHeader
import play.api.libs.iteratee.Enumerator
import play.api.http._
import scala.concurrent.Future
import play.api.http.Status._
import scala.concurrent.ExecutionContext

trait JsonResults extends JsonResultAttribues {
  val JsonOk = new JsonResult(OK)
  val JsonBadRequest = new JsonResult(BAD_REQUEST)
}

trait JsonResultAttribues {
  val jsonSuccess = "success"
  val jsonError = "error"
}

class JsonResult(status: Int)
    extends SimpleResult[Results.EmptyContent](
      header = ResponseHeader(status),
      body = Enumerator(Results.EmptyContent()))
    with JsonResultAttribues {

  val isSuccess = List(OK) contains status

  def createResult(content: JsObject)(implicit writeable: Writeable[JsObject]) =
    SimpleResult(
      header = ResponseHeader(
        status,
        writeable
          .contentType
          .map(ct => Map(HeaderNames.CONTENT_TYPE -> ct))
          .getOrElse(Map.empty)),
      Enumerator(content))

  def apply(html: Html, messages: Seq[(String, String)]) =
    createResult(jsonHTMLResult(html, messages))

  def apply(html: Html, message: String) = {
    if (isSuccess)
      createResult(jsonHTMLResult(html, Seq(jsonSuccess -> message)))
    else
      createResult(jsonHTMLResult(html, Seq(jsonError -> message)))
  }

  def apply(json: JsObject, message: String) =
    createResult(json ++ jsonMessages(Seq(jsonSuccess -> message)))

  def apply(message: String): SimpleResult[JsObject] =
    apply(Html.empty, message)

  def jsonHTMLResult(html: Html, messages: Seq[(String, String)]) = {
    val htmlJson = html.body match {
      case "" =>
        Json.obj()
      case body =>
        Json.obj("html" -> body)
    }

    htmlJson ++ jsonMessages(messages)
  }

  def jsonMessages(messages: Seq[(String, String)]) =
    Json.obj(
      "messages" -> messages.map(m => Json.obj(m._1 -> m._2)))
}