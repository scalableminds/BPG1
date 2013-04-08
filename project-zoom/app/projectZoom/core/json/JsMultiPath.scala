package projectZoom.core.json

import play.api.libs.json.JsPath
import play.api.libs.json.Reads
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

case class JsMultiPath(paths: List[JsPath]) {
  def fail[T] = 
    Reads[T]{ _ => JsError(JsPath, ValidationError("validate.error.missing-path"))}
  
  def apply[T]( f: JsPath => Reads[T]) = {
    def orAll(r: Reads[T], paths: Seq[JsPath]): Reads[T] = {
      paths match{
        case head :: tail =>
          orAll(r or f(head), tail)
        case _ =>
          r
      }
    }
    
    orAll(fail, paths)
  }
}