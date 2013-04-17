package projectZoom.core.knowledge

import akka.actor.Actor
import java.io.File
import play.api.libs.json.JsValue
import projectZoom.util.DBCollection
import java.io.InputStream
import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api.Logger
import projectZoom.core.event._
import projectZoom.util.StartableActor
import models.Project
import models.UserLike
import models.User
import play.api.libs.concurrent.Execution.Implicits._
import models.UserDAO

case class ProjectFound(project: Project)
case class ProjectAggregation(l: List[ProjectFound])

case class UserFound(user: UserLike)
case class UserAggregation(l: List[UserFound])

class KnowledgeActor extends EventSubscriber with EventPublisher {

  def handleUserFound(userLike: UserLike) = {
    UserDAO.findByEmail(userLike.email).map {
      case None => UserDAO.allowRegistration(userLike)
      case _    =>
    }
  }

  def receive = {
    case UserFound(userLike)          => handleUserFound(userLike)

    case UserAggregation(users)       => users.map(u => handleUserFound(u.user))

    case ProjectFound(project)        =>
    //TODO: handle

    case ProjectAggregation(projects) =>
    //TODO: handle
  }

}

object KnowledgeActor extends StartableActor[KnowledgeActor] {
  def name = "knowledgeActor"
}