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
import models.ProjectLike
import models.User
import play.api.libs.concurrent.Execution.Implicits._
import models.Profile
import models.ProfileDAO
import models.ProjectDAO
import models.GlobalDBAccess

case class ProjectFound(project: ProjectLike) extends Event
case class ProjectAggregation(l: List[ProjectFound]) extends Event

case class ProfileFound(profile: Profile) extends Event
case class ProfileAggregation(l: List[ProfileFound]) extends Event

class KnowledgeActor extends EventSubscriber with EventPublisher with GlobalDBAccess{

  def handleProfileAggregation(foundProfiles: List[ProfileFound]) = {
    ProfileDAO.findAll.map { dbProfiles =>
      val found = foundProfiles.map(_.profile)

      dbProfiles
        .flatMap(ProfileDAO.profileFormat.reads(_).asOpt)
        .filterNot(found.contains)
        .map(handleProfileDelete)

      foundProfiles.map {
        case ProfileFound(profile) =>
          handleProfileFound(profile)
      }
    }
  }

  def handleProjectAggregation(foundProjects: List[ProjectFound]) = {
    ProjectDAO.findAll.map { dbProjects =>
      val found = foundProjects.map(_.project)

      dbProjects
        .flatMap(ProjectDAO.projectLikeFormat.reads(_).asOpt)
        .filterNot(found.contains)
        .map(handleProjectDelete)

      foundProjects.map {
        case ProjectFound(project) =>
          handleProjectFound(project)
      }
    }
  }

  def handleProfileDelete(profile: Profile) = {
    // TODO: handle
  }
  def handleProjectDelete(project: ProjectLike) = {
    // TODO: handle
  }

  def handleProfileUpdate(profile: Profile) = {
    ProfileDAO.update(profile).map { lastError =>
      if (lastError.updated > 0) {
        //i f (lastError.updatedExisting)
        //publish(ArtifactUpdated(artifactInfo))
        //else
        //publish(ArtifactInserted(artifactInfo))
      }
    }
  }

  def handleProjectFound(project: ProjectLike) = {
    handleProjectUpdate(project)
  }

  def handleProjectUpdate(project: ProjectLike) = {
    ProjectDAO.update(project).map { lastError =>
      if (lastError.updated > 0) {
        // TODO: do something?
      }
    }
  }

  def handleProfileFound(profile: Profile) = {
    handleProfileUpdate(profile)
    ProfileDAO.findOneByConnectedEmail(profile.email).map {
      case None =>
        ProfileDAO.allowRegistration(profile)
      case _ =>
    }
  }

  def receive = {
    case ProfileFound(profile)        => handleProfileFound(profile)
 
    case ProfileAggregation(profiles) => handleProfileAggregation(profiles)

    case ProjectFound(project)        => handleProjectFound(project)

    case ProjectAggregation(projects) => handleProjectAggregation(projects)
  }

}

object KnowledgeActor extends StartableActor[KnowledgeActor] {
  def name = "knowledgeActor"
}