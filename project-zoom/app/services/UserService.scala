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
import models.ProfileDAO
import play.api.libs.concurrent.Execution.Implicits._
import models.UserHelpers

/**
 * A Sample In Memory user service in Scala
 *
 * IMPORTANT: This is just a sample and not suitable for a production environment since
 * it stores everything in memory.
 */
class UserService(application: Application) extends UserServicePlugin(application) {
  def find(id: UserId): Option[User] = {
    Await.result(
      ProfileDAO
        .findOneByUserId(id)
        .map(_.flatMap(_.user)), 5 seconds)
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[User] = {
    Await.result(
      ProfileDAO
        .findOneByEmailAndProvider(email, providerId)
        .map(_.flatMap(_.user)), 5 seconds)
  }

  def save(identity: Identity): User = {
    val user = UserHelpers.fromIdentity(identity)
    user.email.map { email =>
      ProfileDAO.findOneByConnectedEmail(email).map {
        case Some(p) =>
          ProfileDAO.update(p, p.copy(user = Some(user)))
        case _ =>
          Logger.error("Couldn't insert user because no corresponding profile was found")
      }
    }
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