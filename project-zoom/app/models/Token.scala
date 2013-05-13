package models

import securesocial.core.providers.{ Token => SocialToken }
import play.api.libs.json._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import reactivemongo.bson.BSONDateTime
import play.modules.reactivemongo.json.BSONFormats._
import play.api.libs.concurrent.Execution.Implicits._

object Token extends UnsecuredMongoDAO[SocialToken]{
  val collectionName = "tokens"

  def findByAccessToken(accessToken: String) = findHeadOption("accessToken", accessToken)

  override def removeById(id: String)(implicit ctx: models.DBAccessContext) = {
    collectionRemove(Json.obj("uuid" -> id))
  }
  
  override def findOneById(id: String)(implicit ctx: models.DBAccessContext)= {
    collectionFind(Json.obj("uuid" -> id)).one[SocialToken]
  }
  
  def removeExpiredTokens(implicit ctx: models.DBAccessContext) = {
    val now = System.currentTimeMillis()
    collectionRemove(Json.obj("expirationTime" -> Json.obj("$lt" -> BSONDateTime(now))))
  }
  
  implicit val formatter: OFormat[SocialToken] = 
    ((__ \ "uuid").format[String] and
    (__ \ "email").format[String] and
    (__ \ "creationTime").format[DateTime] and
    (__ \ "expirationTime").format[DateTime] and
    (__ \ "isSignUp").format[Boolean])(SocialToken.apply, unlift(SocialToken.unapply))
    
  /*implicit object handler extends BSONDocumentHandler[SocialToken]{
    def write(token: SocialToken): BSONDocument = {
      BSONDocument(
          "uuid" -> BSONString(token.uuid),
          "email" -> BSONString(token.email),
          "creationTime" -> BSONDateTime(token.creationTime.getMillis()),
          "expirationTime" -> BSONDateTime(token.expirationTime.getMillis()),
          "isSignUp" -> BSONBoolean(token.isSignUp)
      )
    }
    def read(doc: BSONDocument): SocialToken = {
      SocialToken(
        doc.getAs[BSONString]("uuid").get.value,
        doc.getAs[BSONString]("email").get.value,
        new DateTime(doc.getAs[BSONDateTime]("creationTime").get.value),
        new DateTime(doc.getAs[BSONDateTime]("expirationTime").get.value),
        doc.getAs[BSONBoolean]("isSignUp").get.value)
    }
  }*/
}
