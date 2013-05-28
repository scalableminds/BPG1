package projectZoom.core.json

import play.api.libs.json.JsPath
import play.api.libs.json.Reads
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

case class JsMultiPath(paths: List[JsPath]) {
  def fail[T] = 
    Reads[T]{ _ => JsError(JsPath, ValidationError("validate.error.missing-path"))}
  
  def simpleUpdate(f: JsPath => Reads[JsObject])(path: JsPath): Reads[JsObject] = {
    def r(current: JsPath, remaining: JsPath): Reads[JsObject] = {
      remaining.path match {
        case p :: IdxPathNode(i) :: tail =>
          JsPath(current.path :+ p).json.update(
            of[JsArray].flatMap[JsArray] {
              case JsArray(l) if (i < l.size) =>
                Reads( _ =>
                  l(i) transform simpleUpdate(f)(JsPath(tail)).map { u =>
                    JsArray(l.updated(i, u))
                  })
              case _ =>
                Reads(_ => 
                  JsError(Seq(remaining -> Seq(ValidationError("validate.error.missing-path")))))
          })
        case p :: tail =>
          r(JsPath(current.path :+ p), JsPath(tail))
        case Nil =>
          f(current)
      }
    }
    
    r(JsPath(), path)
  }
  
  def readWith[T]( f: JsPath => Reads[T]) = {
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
  
  def updateWith( f: JsPath => Reads[JsObject]) = {
    readWith(simpleUpdate(f))
  }
}