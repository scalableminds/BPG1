package models

import securesocial.core.providers.{ Token => SocialToken }
import play.api.libs.json._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import reactivemongo.bson.BSONDateTime
import play.modules.reactivemongo.json.BSONFormats._

object Token extends MongoDAO[SocialToken]{
  val collectionName = "tokens"

  def findByAccessToken(accessToken: String) = findHeadOption("accessToken", accessToken)

  override def removeById(id: String) = {
    collection.remove(Json.obj("uuid" -> id))
  }
  
  override def findById(id: String)= {
    collection.find(Json.obj("uuid" -> id)).one[SocialToken]
  }
  
  def removeExpiredTokens() = {
    val now = System.currentTimeMillis()
    collection.remove(Json.obj("expirationTime" -> Json.obj("$lt" -> BSONDateTime(now))))
  }
  
  implicit val formatter: Format[SocialToken] = 
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
