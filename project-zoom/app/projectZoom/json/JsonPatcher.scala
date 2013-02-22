package projectZoom.json

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Json._
import play.api.libs.json.util._
import play.api.libs.functional.syntax._
import projectZoom.util.ExtendedTypes._
import play.api.data.validation.ValidationError

trait JsonPatch {

  def patch: Reads[JsObject]

  def splitPath(path: String) = {
    val s = path.split("/")
    if (s.size > 1)
      s.dropRight(1).mkString("/") -> s.last
    else
      "" -> path
  }
}

object JsonPatch {
  def equalReads[T](v: T)(implicit r: Reads[T]): Reads[T] = 
    Reads.filter(ValidationError("validate.error.expected.value", v))(_ == v)

  def verifyPath(implicit r: Reads[String]): Reads[String] = 
    Reads.filter(ValidationError("validate.error.invalidPath"))( p =>
      p == "" ||
      p.startsWith("/") && p.split("/").drop(1).forall(pE => pE.size > 0))
  
  def verifyOperation(op: String) =
    (__ \ "op").read[String](equalReads(op))

  def path =
    (__ \ "path").read[String](verifyPath)

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

  val patchRead =
    testPatch or removePatch or addPatch or replacePatch

  def patchesRead =
    __.read(list(patchRead))
    
  def applyPatches(obj: JsObject, patches: Seq[JsonPatch]) = {
    def patchTillFailure(patches: Seq[JsonPatch])(obj: JsObject): JsResult[JsObject] = {
      patches match {
        case patcher :: tail =>
          patcher.patch.reads(obj).flatMap{ o =>
            println("ZR: " + Json.stringify(o))
            patchTillFailure(tail)(o)
          }
        case _ =>
          JsSuccess(obj)
      }
    }
    patchTillFailure(patches)(obj)
  }  
  
  def patch(obj: JsObject, patchObj: JsValue) = 
    JsonPatch.patchesRead.reads(patchObj).flatMap(patches => applyPatches(obj, patches))
}

case class JsonTest(path: String, value: JsValue) extends JsonPatch {

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
    val (parentPath, last) = splitPath(path)

    val verifyObject = (__ \~ path)(_.read[JsValue](JsonPatch.equalReads[JsValue](value)))

    val verifyArray = (__ \~ parentPath)(_.read(equalArrayElementReads(value, last)))

     ( verifyObject orElse verifyArray) andKeep __.json.pick[JsObject]
  }
}

case class JsonRemove(path: String) extends JsonPatch {
  def patch = {
    val (parentPath, last) = splitPath(path)

    val removeFromObject = (__ \~ path)(_.json.prune)

    val removeFromArray = (__ \~ parentPath)(_.json.update(of[JsArray] map {
      case JsArray(l) =>
        val updated = {
          last.toIntOpt match {
            case Some(idx) if idx < l.size =>
              val (front, back) = l.splitAt(idx)
              front ++ back.drop(1)
            case _ =>
              l
          }
        }
        JsArray(updated)
    }))

    (__ \~ path)(_.json.pick) andKeep (removeFromObject orElse removeFromArray)
  }
}

case class JsonReplace(path: String, value: JsValue) extends JsonPatch {
  def patch = {
    val (parentPath, last) = splitPath(path)

    val updateObject = (__ \ last).read[JsValue] andThen (__ \ last).json.put(value)

    val updateArray = of[JsArray].flatMap[JsArray] {
      case JsArray(l) if (last.toIntOpt.map(_ < l.size).getOrElse(false)) =>
        Reads.pure(JsArray(l.updated(last.toInt, value)))
      case _ =>
        Reads(_ => JsError(__, ValidationError("validate.error.missing-path", Seq(""))))
    }

    (__ \~ parentPath)(_.json.update(updateArray)) orElse (__ \~ parentPath)(_.json.update(updateObject))
  }
}

case class JsonAdd(path: String, value: JsValue) extends JsonPatch {
  def patch = {
    val (parentPath, last) = splitPath(path)

    val updateObject = __.json.pick[JsObject] andThen ((__ \ last).json.put(value))

    val updateArray = of[JsArray] map {
      case JsArray(l) =>
        val updated = {
          if (last == "-")
            l :+ value
          else {
            last.toIntOpt match {
              case Some(idx) if idx < l.size =>
                l.updated(idx, value)
              case _ =>
                l
            }
          }
        }
        JsArray(updated)
    }

    (__ \~ parentPath)(_.json.update(updateArray)) orElse (__ \~ parentPath)(_.json.update(updateObject))
  }
}