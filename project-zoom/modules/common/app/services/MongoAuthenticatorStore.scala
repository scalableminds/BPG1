package services

import securesocial.core.AuthenticatorStore
import securesocial.core.Authenticator
import models.UserCookieDAO
import scala.concurrent.Await
import scala.concurrent.duration._
import models.GlobalDBAccess

class MongoAuthenticatorStore(app: play.api.Application) extends AuthenticatorStore(app) with GlobalDBAccess{
  def save(authenticator: Authenticator): Either[Error, Unit] = {
    UserCookieDAO.refreshCookie(authenticator)
    Right(())
  }
  
  def find(id: String): Either[Error, Option[Authenticator]] = {
    Right(Await.result(UserCookieDAO.findHeadOption("id", id), 5 seconds))
  }
  
  def delete(id: String): Either[Error, Unit] = {
    UserCookieDAO.remove("id", id)
    Right(())
  }
}