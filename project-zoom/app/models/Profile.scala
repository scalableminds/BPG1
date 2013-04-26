package models

import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.JsObject
import play.modules.reactivemongo.json.BSONFormats._
import securesocial.core.UserId
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger

case class Profile(firstName: String, lastName: String, email: String, linkedEmails: List[String] = Nil, user: Option[User] = None)

object ProfileDAO extends MongoJsonDAO with UserHelpers {
  val collectionName = "profiles"

  implicit val profileFormat = Json.format[Profile]

  def allowRegistration(profile: Profile) = {
    //TODO: send email with registration information
  }

  override def findOneById(id: String) = {
    findByEither("email" -> Some.apply _, "_id" -> toMongoObjectIdString _)(id).one[JsObject]
  }

  def findOneByConnectedEmail(email: String) = {
    collection.find(Json.obj(
      "$or" -> Json.arr(
        Json.obj("email" -> email),
        Json.obj("linkedEmails" -> email)))).one[Profile]
  }

  def findOneByUserId(userId: UserId) = {
    collection.find(Json.obj("user.id" -> userId)).one[Profile]
  }

  def update(p: Profile) =
    collection.update(Json.obj("email" -> p.email),
      Json.obj("$set" -> p), upsert = true)

  def update(p: Profile, u: Profile) = {
    Logger.warn(Json.toJson(u).toString)
    collection.update(Json.obj("email" -> p.email), u, upsert = true, multi = false)
  }

  def findOneByEmailAndProvider(email: String, provider: String) = {
    collection.find(Json.obj("user.email" -> email, "user.id.providerId" -> provider)).one[Profile]
  }

}