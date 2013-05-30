package projectZoom.util

import scala.concurrent.Future

object ExtendedTypes {

  implicit class ExtendedFuture[T](f: Future[Future[T]]) {
    import play.api.libs.concurrent.Execution.Implicits._
    
    def flatten: Future[T] = {
      for{
        fs <- f
        fv <- fs
      } yield {
        fv
      }
    }
  }
  
  import play.api.libs.json.JsObject
  import play.api.libs.json.JsValue
  import projectZoom.core.json.JsonPatch

  implicit class ExtendedJsObject(obj: JsObject) {
    def patchWith(patch: JsValue) = {
      JsonPatch.patch(obj, patch)
    }
  }

  implicit class ExtendedString(val s: String) extends AnyVal {

    def toFloatOpt = try {
      Some(s.toFloat)
    } catch {
      case _: java.lang.NumberFormatException => None
    }

    def toIntOpt = try {
      Some(s.toInt)
    } catch {
      case _: java.lang.NumberFormatException => None
    }
  }
}