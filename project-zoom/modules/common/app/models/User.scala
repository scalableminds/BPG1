package models

import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONWriter
import securesocial.core._
import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONInteger
import reactivemongo.bson.BSONHandler
import reactivemongo.bson.BSONHandler
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Part of the information stored in user is a duplicated in profile. 
 * This is due to the interface requirements of ´´Identity´´. A User instance
 * stores all the information needed to authenticate and authorize a user.
 */
case class User(
    id: UserId, 
    firstName: String,
    lastName: String,
    email: Option[String],
    authMethod: AuthenticationMethod,
    oAuth1Info: Option[OAuth1Info],
    oAuth2Info: Option[OAuth2Info],
    passwordInfo: Option[PasswordInfo],
    roles: List[String]) extends Identity {

  val fullName: String = s"$firstName $lastName"
  val avatarUrl = None
}

object UserHelpers extends UserHelpers

trait UserHelpers{

  def fromIdentity(i: Identity): User = {
    User(i.id, i.firstName, i.lastName, i.email, i.authMethod, i.oAuth1Info, i.oAuth2Info, i.passwordInfo, Nil)
  }

  implicit val AuthenticationMethodFormat: Format[AuthenticationMethod] =
    Format(Reads.StringReads.map(AuthenticationMethod.apply), Writes{am: AuthenticationMethod => Writes.StringWrites.writes(am.method)})
  
  implicit val OAuth1InfoFormat: Format[OAuth1Info] = Json.format[OAuth1Info]

  implicit val OAuth2InfoFormat: Format[OAuth2Info] = Json.format[OAuth2Info]

  implicit val PasswordInfoFormat: Format[PasswordInfo] = Json.format[PasswordInfo]

  implicit val UserIdFormat: Format[UserId] = Json.format[UserId]

  implicit val userFormat: Format[User] = Json.format[User]
}
