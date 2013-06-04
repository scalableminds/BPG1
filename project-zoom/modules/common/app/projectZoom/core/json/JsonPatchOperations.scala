package projectZoom.core.json

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.data.validation.ValidationError
import projectZoom.util.ExtendedTypes.ExtendedString

trait JsonPatchOperation {

  def path: JsonPatchPath

  def patch: Reads[JsObject]
}

case class JsonTest(path: JsonPatchPath, value: JsValue) extends JsonPatchOperation {

  def equalArrayElementReads(v: JsValue, last: String)(implicit r: Reads[JsValue]): Reads[JsValue] = {
    Reads.filter(ValidationError("validate.error.expected.value", v)) {
      case JsArray(l) =>
        last.toIntOpt match {
          case Some(idx) if idx >= 0 && idx < l.size =>
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

case class JsonRemove(path: JsonPatchPath) extends JsonPatchOperation {
  def patch = {
    val removeFromObject = path.updateWith(_.json.prune)
    val removeFromArray = path.parent.updateWith(_.json.update(of[JsArray] map {
      case JsArray(l) =>
        val updated = {
          path.last.toString.toIntOpt match {
            case Some(idx) if idx >= 0 && idx < l.size =>
              val (front, back) = l.splitAt(idx)
              front ++ back.drop(1)
            case _ =>
              l
          }
        }
        JsArray(updated)
    }))

    path.readWith(_.json.pick) andKeep
      (removeFromArray orElse removeFromObject)
  }
}

case class JsonReplace(path: JsonPatchPath, value: JsValue) extends JsonPatchOperation {
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

case class JsonAdd(path: JsonPatchPath, value: JsValue) extends JsonPatchOperation {
  def patch = {

    val updateObject = __.json.pick[JsObject] andThen (__ \ path.last.toString).json.put(value)
        
    val updateArray = Reads.apply {
      case JsArray(l) =>
        if (path.last.toString == "-")
          JsSuccess(JsArray(l :+ value))
        else {
          path.last.toString.toIntOpt match {
            case Some(idx) if idx >= 0 && idx <= l.size =>
              val (pre, post) = l.splitAt(idx)
              JsSuccess(JsArray((pre :+ value) ++ post))
            case _ =>
              JsError()
          }
        }
      case _ =>
        JsError()
    }

    path.parent.updateWith(_.json.update(updateArray)) orElse
      path.parent.updateWith(_.json.update(updateObject))
  }
}

case class JsonMove(path: JsonPatchPath, from: JsonPatchPath) extends JsonPatchOperation {
  def patch = {
    val valueReads = from.readWith(_.read[JsValue])
    valueReads.flatMap(value => JsonRemove(from).patch andThen JsonAdd(path, value).patch)
  }
}

case class JsonCopy(path: JsonPatchPath, from: JsonPatchPath) extends JsonPatchOperation {
  def patch = {
    val valueReads = from.readWith(_.read[JsValue])
    valueReads.flatMap(value => JsonAdd(path, value).patch)
  }
}