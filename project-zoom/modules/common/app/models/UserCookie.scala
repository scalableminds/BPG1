package models

import securesocial.core.Authenticator
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import securesocial.core.UserId
import org.joda.time.DateTime
import play.api.Logger

object UserCookieDAO extends UnsecuredMongoDAO[Authenticator] {
    /**
   * Name of the DB collection
   */
  val collectionName = "userCookies"
  
  def refreshCookie(a: Authenticator) = {
    collectionUpdate(Json.obj("id" -> a.id), formatter.writes(a), true, false)
  }  
    
  import UserHelpers._
  
  implicit val formatter: OFormat[Authenticator] = 
    ((__ \ "id").format[String] and
    (__ \ "userId").format[UserId] and
    (__ \ "creationDate").format[DateTime] and
    (__ \ "lastUsed").format[DateTime] and
    (__ \ "expirationDate").format[DateTime])(Authenticator.apply _, unlift(Authenticator.unapply))
}