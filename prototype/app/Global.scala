import org.joda.time.DateTime
import com.mongodb.casbah.commons.conversions.scala._
import play.api._

object Global extends GlobalSettings{
  override def onStart(app: Application){
    RegisterJodaTimeConversionHelpers()
    RegisterConversionHelpers()
  }
}