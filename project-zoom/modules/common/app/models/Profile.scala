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
import scala.concurrent.Future
import reactivemongo.core.commands.LastError

case class Profile(firstName: String, lastName: String, email: String, linkedEmails: List[String] = Nil, user: Option[User] = None)

object ProfileDAO extends SecuredMongoJsonDAO[Profile] with UserHelpers {
    /**
   * Name of the DB collection
   */
  val collectionName = "profiles"

  implicit val profileFormat = Json.format[Profile]
  
  val removeSensitiveInformation = 
    (__ \ 'user).json.prune

  def allowRegistration(profile: Profile) = {
    //TODO: send email with registration information
  }

  override def findOneById(id: String)(implicit ctx: DBAccessContext) = {
    findByEither("email" -> Some.apply _, "_id" -> toMongoObjectIdString _)(id).one[JsObject]
  }

  def findOneByConnectedEmail(email: String)(implicit ctx: DBAccessContext) = {
    collectionFind(Json.obj(
      "$or" -> Json.arr(
        Json.obj("email" -> email),
        Json.obj("linkedEmails" -> email)))).one[Profile]
  }

  def findOneByUserId(userId: UserId)(implicit ctx: DBAccessContext) = {
    collectionFind(Json.obj("user.id" -> userId)).one[Profile]
  }

  def update(p: Profile)(implicit ctx: DBAccessContext): Future[LastError] =
    collectionUpdate(Json.obj("email" -> p.email),
      Json.obj("$set" -> p), upsert = true)

  def update(p: Profile, u: Profile)(implicit ctx: DBAccessContext): Future[LastError] = {
    collectionUpdate(Json.obj("email" -> p.email), profileFormat.writes(u), upsert = true, multi = false)
  }

  def findOneByEmailAndProvider(email: String, provider: String)(implicit ctx: DBAccessContext) = {
    collectionFind(Json.obj("user.email" -> email, "user.id.providerId" -> provider)).one[Profile]
  }

}