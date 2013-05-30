package projectZoom.core.json

import org.specs2.mutable.Specification

class JsonPatchTests extends Specification{
  "A path" should{
    "be able to get splitted" in {
      JsonPatchPath("/test/a/b/c").parent should_== JsonPatchPath("/test/a/b/c")
    }
  }
  
  
}