package projectZoom.core.json

import org.specs2.mutable.Specification
import org.specs2.mutable.SpecificationWithJUnit
import java.io.File
import projectZoom.util.JsonHelper
import play.api.libs.json._

class JsonPatchTests extends SpecificationWithJUnit {
  case class JsonSpec(doc: JsValue, patch: JsArray, expected: Option[JsValue], error: Option[String], comment: Option[String], disabled: Option[Boolean])

  implicit val jsonSpecReads = Json.reads[JsonSpec]

  val specsJson = JsonHelper.jsonFromFile(new File("modules/common/test/resources/json_patch_spec.json"))
  "A json patcher" should {
    val testIt = specsJson.asOpt[List[JsonSpec]].map { specs =>
      specs.filter( spec => !(spec.disabled getOrElse false)).map { spec =>
        new InExample(spec.comment orElse spec.error getOrElse "").in {
          JsonPatch.patch(spec.doc, spec.patch) match {
            case JsSuccess(result, _) =>
              spec.expected.map(expected => expected should_== result) getOrElse ko
            case _ =>
              spec.error.map(ok) getOrElse ko
          }
        }

      }
    }
  }
}