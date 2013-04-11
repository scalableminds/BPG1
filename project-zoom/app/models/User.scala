package models

import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONWriter
import securesocial.core._
import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONInteger
import play.modules.reactivemongo.MongoJSONHelpers
import play.modules.reactivemongo.Implicits
import reactivemongo.bson.BSONHandler
import reactivemongo.bson.BSONHandler
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import projectZoom.core.bson.Bson

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

object User extends MongoDAO[User] {
  override def collection = db("users")

  def findByEmail(email: String) = findHeadOption("email", email)

  def findByAccessToken(accessToken: String) = findHeadOption("accessToken", accessToken)

  def allowRegistration(userLike: UserLike) = {
    //TODO: send email with registration information
  }
  
  def findByUserId(userId: UserId) = {
    collection.find(Bson.obj("userId" -> UserIdFormat.writes(userId))).one
  }

  def findByEmailAndProvider(email: String, provider: String) = {
    collection.find(Bson.obj("email" -> email, "userId.providerId" -> provider)).one
  }

  def fromIdentity(i: Identity): User = {
    User(i.id, i.firstName, i.lastName, i.email, i.authMethod, i.oAuth1Info, i.oAuth2Info, i.passwordInfo, Nil)
  }

  implicit val AuthenticationMethodFormat: Format[AuthenticationMethod] = 
    __.format[String].inmap((m: String) => AuthenticationMethod(m), (a: AuthenticationMethod) => a.method)

  implicit val OAuth1InfoFormat: Format[OAuth1Info] = (
    (__ \ 'token).format[String] and
    (__ \ 'secret).format[String])(OAuth1Info.apply, unlift(OAuth1Info.unapply))

  implicit val OAuth2InfoFormat: Format[OAuth2Info] = (
    (__ \ 'accessToken).format[String] and
    (__ \ 'tokenType).formatNullable[String] and
    (__ \ 'expiresIn).formatNullable[Int] and
    (__ \ 'refreshToken).formatNullable[String])(OAuth2Info.apply, unlift(OAuth2Info.unapply))

  implicit val PasswordInfoFormat: Format[PasswordInfo] = (
    (__ \ 'hasher).format[String] and
    (__ \ 'password).format[String] and
    (__ \ 'salt).formatNullable[String])(PasswordInfo.apply, unlift(PasswordInfo.unapply))

  implicit val UserIdFormat: Format[UserId] = (
    (__ \ 'id).format[String] and
    (__ \ 'providerId).format[String])(UserId.apply, unlift(UserId.unapply))

  val userFormat = (
    (__ \ 'userId).format[UserId] and
    (__ \ 'firstName).format[String] and
    (__ \ 'lastName).format[String] and
    (__ \ 'email).formatNullable[String] and
    (__ \ 'authMethod).format[AuthenticationMethod] and
    (__ \ 'oAuth1Info).formatNullable[OAuth1Info] and
    (__ \ 'oAuth2Info).formatNullable[OAuth2Info] and
    (__ \ 'passwordInfo).formatNullable[PasswordInfo] and
    (__ \ 'roles).format(list[String]))(User.apply _, unlift(User.unapply))

  implicit object handler extends BSONDocumentHandler[User] {
    def read(doc: BSONDocument): User =
      userFormat.reads(MongoJSONHelpers.toJSON(doc)).get

    def write(u: User): BSONDocument =
      Implicits.JsObjectWriter.write(userFormat.writes(u))
  }
}
