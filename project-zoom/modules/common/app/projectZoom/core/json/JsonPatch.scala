package projectZoom.core.json

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Json._
import play.api.libs.json.util._
import play.api.libs.functional.syntax._
import projectZoom.util.ExtendedTypes._
import play.api.data.validation.ValidationError
import play.api.Logger

case class JsonPatchDocument(patches: List[JsonPatchOperation]) {
  def patch(obj: JsValue) = {
    def patchTillFailure(patches: Seq[JsonPatchOperation])(obj: JsValue): JsResult[JsValue] = {
      patches match {
        case patchOperation :: tail =>
          patchOperation.patch.reads(obj).flatMap { o =>
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

  def from =
    (__ \ "from").read[String](verifyPath).map(JsonPatchPath)

  def value =
    (__ \ "value").json.pick

  val addPatch: Reads[JsonPatchOperation] =
    (verifyOperation("add") andKeep path and value)(JsonAdd)

  val removePatch: Reads[JsonPatchOperation] =
    (verifyOperation("remove") andKeep path).map(JsonRemove)

  val testPatch: Reads[JsonPatchOperation] =
    (verifyOperation("test") andKeep path and value)(JsonTest)

  val replacePatch: Reads[JsonPatchOperation] =
    (verifyOperation("replace") andKeep path and value)(JsonReplace)

  val movePatch: Reads[JsonPatchOperation] =
    (verifyOperation("move") andKeep path and from)(JsonMove)

  val copyPatch: Reads[JsonPatchOperation] =
    (verifyOperation("copy") andKeep path and from)(JsonCopy)

  val patchReads: Reads[JsonPatchOperation] =
    testPatch or removePatch or addPatch or replacePatch or movePatch or copyPatch

  def patchDocReads = (__.read(list(patchReads))).map(JsonPatchDocument)

  def patch(obj: JsValue, patchObj: JsValue) = {
    patchDocReads.reads(patchObj).flatMap(_.patch(obj))
  }
}