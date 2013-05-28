package projectZoom.core.json

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Json._
import play.api.libs.json.util._
import play.api.libs.functional.syntax._
import projectZoom.util.ExtendedTypes._
import play.api.data.validation.ValidationError
import play.api.Logger

trait JsonPatch {

  def path: JsonPatchPath

  def patch: Reads[JsObject]
}

case class JsonPatchDoc(patches: List[JsonPatch]) {
  def patch(obj: JsObject) = {
    def patchTillFailure(patches: Seq[JsonPatch])(obj: JsObject): JsResult[JsObject] = {
      patches match {
        case patcher :: tail =>
          patcher.patch.reads(obj).flatMap { o =>
            Logger.trace("ZR: " + Json.stringify(o))
            patchTillFailure(tail)(o)
          }
        case _ =>
          JsSuccess(obj)
      }
    }
    patchTillFailure(patches)(obj)
  }
}

object JsonPatch {
  def equalReads[T](v: T)(implicit r: Reads[T]): Reads[T] =
    Reads.filter(ValidationError("validate.error.expected.value", v))(_ == v)

  def verifyPath(implicit r: Reads[String]): Reads[String] =
    Reads.filter(ValidationError("validate.error.expected.patchPath"))(p =>
      p == "" ||
        p.startsWith("/") && p.split("/").drop(1).forall(pE => pE.size > 0))

  def verifyOperation(op: String) =
    (__ \ "op").read[String](equalReads(op))

  def path =
    (__ \ "path").read[String](verifyPath).map(JsonPatchPath)

  def value =
    (__ \ "value").json.pick

  val addPatch: Reads[JsonPatch] =
    (verifyOperation("add") andKeep path and value)(JsonAdd)

  val removePatch: Reads[JsonPatch] =
    (verifyOperation("remove") andKeep path).map(JsonRemove)

  val testPatch: Reads[JsonPatch] =
    (verifyOperation("test") andKeep path and value)(JsonTest)

  val replacePatch: Reads[JsonPatch] =
    (verifyOperation("replace") andKeep path and value)(JsonReplace)

  val patchReads: Reads[JsonPatch] =
    testPatch or removePatch or addPatch or replacePatch

  def patchDocReads = (__.read(list(patchReads))).map(JsonPatchDoc)

  def patch(obj: JsObject, patchObj: JsValue) = {
    JsonPatch.patchDocReads.reads(patchObj).flatMap(_.patch(obj))
  }
}

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
        case p :: IdxPathNode(i) :: tail =>
          JsPath(current.path :+ p).json.update(
            of[JsArray].flatMap[JsArray] {
              case JsArray(l) if (i < l.size) =>
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

    r(JsPath(), path)
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

  override def toString = path

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

case class JsonTest(path: JsonPatchPath, value: JsValue) extends JsonPatch {

  def equalArrayElementReads(v: JsValue, last: String)(implicit r: Reads[JsValue]): Reads[JsValue] = {
    Reads.filter(ValidationError("validate.error.expected.value", v)) {
      case JsArray(l) =>
        last.toIntOpt match {
          case Some(idx) if idx < l.size =>
            l(idx) == v
          case _ =>
            false
        }
      case _ =>
        false
    }
  }

  def patch = {

    val verifyObject = path.readWith(_.read[JsValue](JsonPatch.equalReads[JsValue](value)))

    val verifyArray = path.parent.readWith(_.read(equalArrayElementReads(value, path.last.toString)))

    (verifyObject orElse verifyArray) andKeep __.json.pick[JsObject]
  }
}

case class JsonRemove(path: JsonPatchPath) extends JsonPatch {
  def patch = {
    val removeFromObject = path.updateWith(_.json.prune)

    val removeFromArray = path.parent.updateWith(_.json.update(of[JsArray] map {
      case JsArray(l) =>
        val updated = {
          path.last.toString.toIntOpt match {
            case Some(idx) if idx < l.size =>
              val (front, back) = l.splitAt(idx)
              front ++ back.drop(1)
            case _ =>
              l
          }
        }
        JsArray(updated)
    }))

    path.readWith(_.json.pick) andKeep
      (removeFromObject orElse removeFromArray)
  }
}

case class JsonReplace(path: JsonPatchPath, value: JsValue) extends JsonPatch {
  def patch = {
    val updateObject = (__ \ path.last.toString).read[JsValue] andThen (__ \ path.last.toString).json.put(value)
    
    val updateArray = of[JsArray].flatMap[JsArray] {
      case JsArray(l) if (path.last.toString.toIntOpt.map(_ < l.size).getOrElse(false)) =>
        Reads.pure(JsArray(l.updated(path.last.toString.toInt, value)))
      case _ =>
        Reads(_ => JsError(__, ValidationError("validate.error.missing-path", Seq(""))))
    }

    path.parent.updateWith(_.json.update(updateArray)) orElse
      path.parent.updateWith(_.json.update(updateObject))
  }
}

case class JsonAdd(path: JsonPatchPath, value: JsValue) extends JsonPatch {
  def patch = {

    val updateObject = __.json.pick[JsObject] andThen (__ \ path.last.toString).json.put(value)

    val updateArray = Reads.apply {
      case JsArray(l) =>
        if (path.last.toString == "-")
          JsSuccess(JsArray(l :+ value))
        else {
          path.last.toString.toIntOpt match {
            case Some(idx) if idx <= l.size =>
              val (pre, post) = l.splitAt(idx)
              JsSuccess(JsArray((pre :+ value) ++ post))
            case _ =>
              JsError()
          }
        }
      case _ =>
        JsError()
    }

    path.parent.updateWith (_.json.update(updateArray)) orElse
      path.parent.updateWith(_.json.update(updateObject))
  }
}