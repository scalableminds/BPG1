package models

import reactivemongo.bson.handlers.BSONReader
import reactivemongo.bson.BSONDocument
import play.modules.reactivemongo.PlayBsonImplicits._

case class User(id: String, firstName: String, lastName: String, roles: List[String])

object User extends MongoDAO[User] {
  override def collection = db("users")

  override implicit val reader = User.UserBSONReader

  def findByEmail(email: String) = findHeadOption("email", email)

  def findByAccessToken(accessToken: String) = findHeadOption("accessToken", accessToken)

  object UserBSONReader extends BSONReader[User]{
    def fromBSON(doc: BSONDocument): User = ???
  }
}
