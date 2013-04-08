package projectZoom.util

object ExtendedTypes {

  import play.api.libs.json.JsObject
  import play.api.libs.json.JsValue
  import projectZoom.core.json.JsonPatch

  implicit class ExtendedJsObject(obj: JsObject) {
    def patchWith(patch: JsValue) = {
      JsonPatch.patch(obj, patch)
    }
  }

  implicit class ExtendedString(val s: String) extends AnyVal {

    def toFloatOpt = try {
      Some(s.toFloat)
    } catch {
      case _: java.lang.NumberFormatException => None
    }

    def toIntOpt = try {
      Some(s.toInt)
    } catch {
      case _: java.lang.NumberFormatException => None
    }
  }

  import play.api.libs.json.JsPath
  import projectZoom.core.json.JsMultiPath

  implicit class ExtendedJsPath(val p: JsPath) {
    def \~(ps: String): JsMultiPath = {
      if (ps == "" || ps == "/")
        JsMultiPath(List(JsPath))
      else {
        val path = ps.split("/").drop(1).map(_.replace("~1", "/").replace("~0", "~")).toList
        \~(path)
      }
    }

    def \~(ps: List[String]) = {
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
      JsMultiPath(createNextPaths(List(JsPath), ps))
    }
  }
}