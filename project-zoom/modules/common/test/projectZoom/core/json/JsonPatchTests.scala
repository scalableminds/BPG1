package projectZoom.core.json

import org.specs2.mutable.Specification
import org.specs2.mutable.SpecificationWithJUnit

class JsonPatchTests extends SpecificationWithJUnit {
  "A path" should {
    "be able to extract parent" in {
      JsonPatchPath("/test/a/b/c").parent should_== JsonPatchPath("/test/a/b")
      JsonPatchPath("/test/1/a").parent should_== JsonPatchPath("/test/1")
    }
  }
}