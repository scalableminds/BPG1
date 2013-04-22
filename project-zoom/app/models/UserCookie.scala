package models

import securesocial.core.Authenticator
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import securesocial.core.UserId
import org.joda.time.DateTime
import play.api.Logger

object UserCookieDAO extends MongoDAO[Authenticator] {
  val collectionName = "userCookies"
  
  def refreshCookie(a: Authenticator) = {
    update(Json.obj("id" -> a.id), a, true, false)
  }  
    
  import UserDAO._
  val formatter = 
    ((__ \ "id").format[String] and
    (__ \ "userId").format[UserId] and
    (__ \ "creationDate").format[DateTime] and
    (__ \ "lastUsed").format[DateTime] and
    (__ \ "expirationDate").format[DateTime])(Authenticator.apply _, unlift(Authenticator.unapply))
}