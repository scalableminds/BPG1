package models

import reactivemongo.bson.BSONDocument
import reactivemongo.bson.handlers._
import reactivemongo.bson.BSONString
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import securesocial.core._
import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONInteger
import play.api.libs.json.Json
import play.api.libs.functional.syntax._
import play.api.libs.json.Format

case class User(
    id: UserId,
    firstName: String,
    lastName: String,
    email: Option[String],
    authMethod: AuthenticationMethod,
    oAuth1Info: Option[OAuth1Info],
    oAuth2Info: Option[OAuth2Info],
    passwordInfo: Option[PasswordInfo],
    roles: List[String] = Nil) extends Identity {
  val fullName: String = s"$firstName $lastName"
  val avatarUrl = None
}

object User extends MongoDAO[User] {
  override def collection = db("users")

  def findByEmail(email: String) = findHeadOption("email", email)

  def findByAccessToken(accessToken: String) = findHeadOption("accessToken", accessToken)

  def findByUserId(userId: UserId) = {
    collection.find(BSONDocument(
      "id" -> BSONString(userId.id),
      "provider" -> BSONString(userId.providerId))).headOption
  }

  def findByEmailAndProvider(email: String, provider: String) = {
    collection.find(BSONDocument(
      "email" -> BSONString(email),
      "provider" -> BSONString(provider))).headOption
  }

  def apply(i: Identity): User = {
    User(i.id, i.firstName, i.lastName, i.email, i.authMethod, i.oAuth1Info, i.oAuth2Info, i.passwordInfo)
  }

  implicit object reader extends BSONReader[User] {
    def fromBSON(document: BSONDocument): User = {
      val doc = document.toTraversable
      User(
        UserId(doc.getAs[BSONString]("id").get.value,
          doc.getAs[BSONString]("provider").get.value),
        doc.getAs[BSONString]("firstName").get.value,
        doc.getAs[BSONString]("lastName").get.value,
        doc.getAs[BSONString]("email").map(_.value),
        AuthenticationMethod(doc.getAs[BSONString]("authMethod").get.value),
        doc.getAs[BSONDocument]("oAuth1Info").map { oc =>
          val o = oc.toTraversable
          OAuth1Info(
            o.getAs[BSONString]("token").get.value,
            o.getAs[BSONString]("secret").get.value)
        },
        doc.getAs[BSONDocument]("oAuth2Info").map { oc =>
          val o = oc.toTraversable
          OAuth2Info(
            o.getAs[BSONString]("accessToken").get.value,
            o.getAs[BSONString]("tokenType").map(_.value),
            o.getAs[BSONInteger]("expiresIn").map(_.value),
            o.getAs[BSONString]("refreshToken").map(_.value))
        },
        doc.getAs[BSONDocument]("passwordInfo").map { oc =>
          val o = oc.toTraversable
          PasswordInfo(
            o.getAs[BSONString]("hasher").get.value,
            o.getAs[BSONString]("password").get.value,
            o.getAs[BSONString]("salt").map(_.value))
        },
        doc.getAs[BSONArray]("roles").get.iterator.map(_.value.toString).toList)
    }
  }

  implicit object writer extends BSONWriter[User] {
    def toBSON(token: User): BSONDocument = ???
  }
}
