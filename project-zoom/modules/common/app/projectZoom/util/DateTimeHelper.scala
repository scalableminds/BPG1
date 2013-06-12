package projectZoom.util

import play.api.libs.json._
import org.joda.time.DateTime

object DateTimeHelper {
  val boxPattern = "yyyy-MM-dd'T'HH:mm:ssZ" 
  implicit val BoxTimeStampReader: Reads[DateTime] = Reads.jodaDateReads(boxPattern, x => x)
}