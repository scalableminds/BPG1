package projectZoom.core.json

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.data.validation.ValidationError
import projectZoom.util.ExtendedTypes.ExtendedString

case class JsonPatchPath(private val path: String) {
  val splittedPath = {
    val s = path.split("/")
    if (s.size > 1)
      s.dropRight(1).mkString("/") -> s.last
    else
      "" -> path
  }

  val paths = extractPossiblePatchPaths(path)

  def parent: JsonPatchPath = JsonPatchPath(splittedPath._1)
  def last: JsonPatchPath = JsonPatchPath(splittedPath._2)

  def fail[T] =
    Reads[T] { _ => JsError(JsPath, ValidationError("validate.error.missing-path")) }

  private def simpleUpdate(f: JsPath => Reads[JsObject])(path: JsPath): Reads[JsObject] = {
    def r(current: JsPath, remaining: JsPath): Reads[JsObject] = {
      remaining.path match {
        case IdxPathNode(i) :: tail =>
          println("idxp")
          JsPath(current.path).json.update(
            of[JsArray].flatMap[JsArray] {
              case JsArray(l) if (i >= 0 &&i < l.size) =>
                Reads(_ =>
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
    println("reached with path: " + path)
    r(JsPath, path)
  }

  def readWith[T](f: JsPath => Reads[T]) = {
    def orAll(r: Reads[T], paths: Seq[JsPath]): Reads[T] = {
      paths match {
        case head :: tail =>
          orAll(r or f(head), tail)
        case _ =>
          r
      }
    }

    orAll(fail, paths)
  }

  def updateWith(f: JsPath => Reads[JsObject]) = {
    readWith(simpleUpdate(f))
  }

  override def toString = 
    if(path.startsWith("/"))
      path.drop(1)
    else
      path

  private def extractPossiblePatchPaths(ps: List[String]): List[JsPath] = {
    def createNextPaths(results: List[JsPath], path: List[String]): List[JsPath] = path match {
      case pathSegment :: tail =>
        pathSegment.toIntOpt match {
          case Some(idx) =>
            createNextPaths(results.map(_(idx)) ++ results.map(_ \ pathSegment), tail)
          case _ =>
            createNextPaths(results.map(_ \ pathSegment), tail)
        }

      case _ =>
        results
    }
    createNextPaths(List(JsPath), ps)
  }

  private def extractPossiblePatchPaths(ps: String): List[JsPath] = {
    if (ps == "" || ps == "/")
      List(JsPath)
    else {
      val pathSegments =
        ps.split("/").tail.map(_.replace("~1", "/").replace("~0", "~")).toList
      extractPossiblePatchPaths(pathSegments)
    }
  }
}