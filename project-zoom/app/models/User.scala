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

case class UserLike(firstName: String, lastName: String, email: String)

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

object UserDAO extends MongoDAO[User] {
  val collectionName = "users"

  def findOneByEmail(email: String) = findHeadOption("email", email)

  def findOneByAccessToken(accessToken: String) = findHeadOption("accessToken", accessToken)

  def allowRegistration(userLike: UserLike) = {
    //TODO: send email with registration information
  }

  def findOneByUserId(userId: UserId) = {
    collection.find(Json.obj("id" -> userId)).one[User]
  }

  def findOneByEmailAndProvider(email: String, provider: String) = {
    collection.find(Json.obj("email" -> email, "userId.providerId" -> provider)).one[User]
  }

  def fromIdentity(i: Identity): User = {
    User(i.id, i.firstName, i.lastName, i.email, i.authMethod, i.oAuth1Info, i.oAuth2Info, i.passwordInfo, Nil)
  }

  implicit val AuthenticationMethodFormat: Format[AuthenticationMethod] =
    Format(Reads.StringReads.map(AuthenticationMethod.apply), Writes{am: AuthenticationMethod => Writes.StringWrites.writes(am.method)})
  
  //.apply(inmap((m: String) => AuthenticationMethod(m), (a: AuthenticationMethod) => a.method)

  implicit val OAuth1InfoFormat: Format[OAuth1Info] = Json.format[OAuth1Info]

  implicit val OAuth2InfoFormat: Format[OAuth2Info] = Json.format[OAuth2Info]

  implicit val PasswordInfoFormat: Format[PasswordInfo] = Json.format[PasswordInfo]

  implicit val UserIdFormat: Format[UserId] = Json.format[UserId]

  implicit val formatter: Format[User] = Json.format[User]
}
