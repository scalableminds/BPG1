package projectZoom.util

import java.io.File
import scala.io.Source
import play.api.libs.json._

object JsonHelper {
  def jsonFromFile(file: File) = 
    try{
      Json.parse(Source.fromFile(file).getLines.mkString)
    } catch {
      case e: java.io.EOFException =>
        System.err.println("JsonHelper: EOFException")
        Json.obj()
    }
}