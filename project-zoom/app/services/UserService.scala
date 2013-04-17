package services

import play.api.{ Logger, Application }
import securesocial.core._
import securesocial.core.providers.{ Token => SocialToken }
import securesocial.core.UserId
import scala.Some
import org.mindrot.jbcrypt.BCrypt
import models.Token
import scala.concurrent.Await
import scala.concurrent.duration._
import models.User
import models.UserDAO

/**
 * A Sample In Memory user service in Scala
 *
 * IMPORTANT: This is just a sample and not suitable for a production environment since
 * it stores everything in memory.
 */
class UserService(application: Application) extends UserServicePlugin(application) {
  def find(id: UserId): Option[User] = {
    Await.result(UserDAO.findOneByUserId(id), 5 seconds)
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[User] = {
    Await.result(UserDAO.findOneByEmailAndProvider(email, providerId), 5 seconds)
  }

  def save(identity: Identity): User = {
    val user = UserDAO.fromIdentity(identity)
    UserDAO.insert(user)
    user
  }

  def save(token: SocialToken) {
    Token.insert(token)
  }

  def findToken(token: String): Option[SocialToken] = {
    Await.result(Token.findOneById(token), 5 seconds)
  }

  def deleteToken(uuid: String) {
    Token.removeById(uuid)
  }

  def deleteTokens() {
    Token.removeAll()
  }

  def deleteExpiredTokens() {
    Token.removeExpiredTokens()
  }
}