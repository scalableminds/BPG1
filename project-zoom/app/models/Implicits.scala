package models

import securesocial.core.SecuredRequest

object Implicits {
  implicit def requestToDBAccess(implicit request: SecuredRequest[_]): DBAccessContext = {
    request.user match {
      case u: User =>
        AuthedAccessContext(u)
      case _ =>
        UnAuthedAccessContext
    }
  }

}