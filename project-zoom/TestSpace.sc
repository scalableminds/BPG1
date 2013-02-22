object TestSpace {
  import play.api.libs.json.Json
  import play.api.libs.json._
  import projectZoom.util.ExtendedTypes.ExtendedJsPath
  import projectZoom.json._

  val j = Json.arr(
    Json.obj(
      "op" -> "add",
      "path" -> "/test/a/-",
      "value" -> 1),
    Json.obj(
      "op" -> "test",
      "path" -> "/test/d",
      "value" -> 2),
    Json.obj(
      "op" -> "remove",
      "path" -> "/test/b"),
    Json.obj(
      "op" -> "replace",
      "path" -> "/test/a/4",
      "value" -> "hello"))                        //> j  : play.api.libs.json.JsArray = [{"op":"add","path":"/test/a/-","value":1}
                                                  //| ,{"op":"test","path":"/test/d","value":2},{"op":"remove","path":"/test/b"},{
                                                  //| "op":"replace","path":"/test/a/4","value":"hello"}]
  val a = Json.obj(
    "test" -> Json.obj(
      "a" -> Json.arr(2,3,4,5,4),
      "b" -> "",
      "d" -> 2,
      "c" -> "x"))                                //> a  : play.api.libs.json.JsObject = {"test":{"a":[2,3,4,5,4],"b":"","d":2,"c"
                                                  //| :"x"}}
  JsonPatch.patch(a, j)                           //> ZR: {"test":{"a":[2,3,4,5,4,1],"b":"","d":2,"c":"x"}}
                                                  //| ZR: {"test":{"a":[2,3,4,5,4,1],"b":"","d":2,"c":"x"}}
                                                  //| ZR: {"test":{"a":[2,3,4,5,4,1],"d":2,"c":"x"}}
                                                  //| ZR: {"test":{"a":[2,3,4,5,"hello",1],"d":2,"c":"x"}}
                                                  //| res0: play.api.libs.json.JsResult[play.api.libs.json.JsObject] = JsSuccess({
                                                  //| "test":{"a":[2,3,4,5,"hello",1],"d":2,"c":"x"}},)
  //a.patch.reads(j)
  //println("Hello")
}