package controllers.common

import play.api.mvc.{ Controller => PlayController }
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
import models.ProvidesSessionData
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.{Controller => PlayController}

class ResultBox[T <: Result](b: Box[T]) {
  import Results.Status
  def asResult = b match {
    case Full(result) =>
      result
    case ParamFailure(msg, _, _, statusCode: Int) =>
      new JsonResult(statusCode)(msg)
    case Failure(msg, _, _) =>
      new JsonResult(BAD_REQUEST)(msg)
    case Empty =>
      new JsonResult(NOT_FOUND)("Couldn't find the requested ressource.")
  }
}

trait BoxImplicits {
  implicit def option2Box[T](in: Option[T]): Box[T] = Box(in)

  implicit def box2Result[T <: Result](b: Box[T]): Result =
    new ResultBox(b).asResult

  implicit def box2ResultBox[T <: Result](b: Box[T]) = new ResultBox(b)

  implicit def futureBox2Result[T <: Result](b: Box[Future[T]])(implicit ec: ExecutionContext): Future[Result] = {
    b match {
      case Full(f) =>
        f.map(value => new ResultBox(Full(value)).asResult)
      case Empty =>
        Future.successful(new ResultBox(Empty).asResult)
      case f: Failure =>
        Future.successful(new ResultBox(f).asResult)
    }
  }
}

class ControllerBase extends PlayController
    with JsonResults
    with BoxImplicits
    with Status
    with ProvidesSessionData {

  def postParameter(parameter: String)(implicit request: Request[Map[String, Seq[String]]]) =
    request.body.get(parameter).flatMap(_.headOption)

  def postParameterList(parameter: String)(implicit request: Request[Map[String, Seq[String]]]) =
    request.body.get(parameter)
}