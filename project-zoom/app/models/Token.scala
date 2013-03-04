package models

import securesocial.core.providers.{ Token => SocialToken }
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.bson._
import reactivemongo.bson.handlers._
import org.joda.time.DateTime
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter

object Token extends MongoDAO[SocialToken] {
  override def collection = db("tokens")

  def findByAccessToken(accessToken: String) = findHeadOption("accessToken", accessToken)

  override def removeById(id: String) = {
    collection.remove(BSONDocument("uuid" -> BSONString(id)))
  }
  
  override def findById(id: String)= {
    collection.find(BSONDocument("uuid" -> BSONString(id))).headOption
  }
  
  def removeExpiredTokens() = {
    val now = System.currentTimeMillis()
    collection.remove(BSONDocument("expirationTime" -> BSONDocument("$lt" -> BSONDateTime(now))))
  }
  
  implicit object reader extends BSONReader[SocialToken] {
    def fromBSON(document: BSONDocument): SocialToken = {
      val doc = document.toTraversable
      SocialToken(
        doc.getAs[BSONString]("uuid").get.value,
        doc.getAs[BSONString]("email").get.value,
        new DateTime(doc.getAs[BSONDateTime]("creationTime").get.value),
        new DateTime(doc.getAs[BSONDateTime]("expirationTime").get.value),
        doc.getAs[BSONBoolean]("isSignUp").get.value)
    }
  }
  
  implicit object writer extends BSONWriter[SocialToken]{
    def toBSON(token: SocialToken): BSONDocument = {
      BSONDocument(
          "uuid" -> BSONString(token.uuid),
          "email" -> BSONString(token.email),
          "creationTime" -> BSONDateTime(token.creationTime.getMillis()),
          "expirationTime" -> BSONDateTime(token.expirationTime.getMillis()),
          "isSignUp" -> BSONBoolean(token.isSignUp)
      )
    }
  }
  
}
