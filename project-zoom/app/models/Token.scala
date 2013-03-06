package models

import securesocial.core.providers.{ Token => SocialToken }
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONBoolean
import org.joda.time.DateTime

object Token extends MongoDAO[SocialToken] {
  override def collection = db("tokens")

  def findByAccessToken(accessToken: String) = findHeadOption("accessToken", accessToken)

  override def removeById(id: String) = {
    collection.remove(BSONDocument("uuid" -> BSONString(id)))
  }
  
  override def findById(id: String)= {
    collection.find(BSONDocument("uuid" -> BSONString(id))).one
  }
  
  def removeExpiredTokens() = {
    val now = System.currentTimeMillis()
    collection.remove(BSONDocument("expirationTime" -> BSONDocument("$lt" -> BSONDateTime(now))))
  }
  
  implicit object handler extends BSONDocumentHandler[SocialToken]{
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
  }
}
