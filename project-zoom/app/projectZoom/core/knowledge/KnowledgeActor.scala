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
import models.User
import play.api.libs.concurrent.Execution.Implicits._
import models.Profile
import models.ProfileDAO

case class ProjectFound(project: Project) extends Event
case class ProjectAggregation(l: List[ProjectFound]) extends Event

case class ProfileFound(profile: Profile) extends Event
case class ProfileAggregation(l: List[ProfileFound]) extends Event

class KnowledgeActor extends EventSubscriber with EventPublisher {

  def handleUserFound(profile: Profile) = {
    ProfileDAO.findOneByConnectedEmail(profile.email).map {
      case None => 
        ProfileDAO.allowRegistration(profile)
      case _    =>
    }
  }

  def receive = {
    case ProfileFound(profile)          => handleUserFound(profile)

    case ProfileAggregation(profiles)       => profiles.map(p => handleUserFound(p.profile))

    case ProjectFound(project)        =>
    //TODO: handle

    case ProjectAggregation(projects) =>
    //TODO: handle
  }

}

object KnowledgeActor extends StartableActor[KnowledgeActor] {
  def name = "knowledgeActor"
}