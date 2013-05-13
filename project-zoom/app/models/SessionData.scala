package models

import play.api.mvc.Flash
import securesocial.core.SecuredRequest
import play.api.mvc.Request
import play.api.mvc.RequestHeader
import securesocial.core.SecureSocial
import securesocial.core.RequestWithUser

case class UnAuthedSessionData(request: RequestHeader) extends SessionData {
  val userOpt = None
}

case class AuthedSessionData(user: User, request: RequestHeader) extends SessionData {
  val userOpt = Some(user)
}

case class UserAwareSessionData(userOpt: Option[User], request: RequestHeader) extends SessionData

trait SessionData {
  def userOpt: Option[User]
  implicit def request: RequestHeader
  def flash: Flash = request.flash
}

trait ProvidesSessionData{

  implicit def sessionDataAuthenticated[A](implicit request: SecuredRequest[A]): AuthedSessionData = {
    AuthedSessionData(request.user.asInstanceOf[User], request)
  }

  implicit def sessionData[A](implicit request: Request[A]): SessionData = {
    request match{
      case r: securesocial.core.RequestWithUser[A] =>
        UserAwareSessionData(r.user.map(_.asInstanceOf[User]), request)
      case _ =>
      UnAuthedSessionData(request)
    }
  }
}