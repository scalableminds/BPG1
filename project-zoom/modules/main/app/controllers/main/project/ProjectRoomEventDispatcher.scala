package controllers.main.project

import controllers.main.GraphUpdated
import projectZoom.core.artifact.ArtifactInserted
import models.ArtifactLike
import models.ProjectDAO
import akka.actor.Actor
import projectZoom.util.StartableActor
import models.Project
import projectZoom.core.artifact.ResourceInserted
import projectZoom.core.event.EventSubscriber
import projectZoom.core.artifact.ArtifactUpdated
import models.ArtifactDAO
import models.GlobalDBAccess
import projectZoom.core.artifact.ResourceUpdated

class ProjectRoomEventDispatcher extends Actor with EventSubscriber with GlobalDBAccess {
  implicit val ec = context.system.dispatcher

  def forwardToProject(_project: String, e: ProjectUpdate) =
    ProjectRoom.roomPlan().get(_project).map { projectActor =>
      projectActor.forward(e)
    }

  def withProject[T](projectName: String)(f: Project => T) = {
    ProjectDAO.findProject(projectName).map(_.map { project =>
      f(project)
    })
  }

  def forwardProjectUpdate(projectName: String, update: ProjectUpdate) = {
    withProject(projectName) { project =>
      forwardToProject(project._id.stringify, update)
    }
  }

  def handleArtifactUpdate(artifact: ArtifactLike) = {
    forwardProjectUpdate(
      artifact.projectName,
      ProjectUpdate("artifacts", "update", ArtifactDAO.artifactLikeWrites.writes(artifact)))
  }

  def handleArtifactInserted(artifact: ArtifactLike) = {
    forwardProjectUpdate(
      artifact.projectName,
      ProjectUpdate("artifacts", "insert", ArtifactDAO.artifactLikeWrites.writes(artifact)))
  }

  def receive = {
    case e: ResourceInserted =>
      handleArtifactUpdate(e.artifact)

    case e: ResourceUpdated =>
      handleArtifactUpdate(e.artifact)

    case e: ArtifactInserted =>
      handleArtifactInserted(e.artifact)

    case e: ArtifactUpdated =>
      handleArtifactUpdate(e.artifact)

    case e: GraphUpdated =>
      println("reached! project: " + e.graph._project.toString)
      forwardToProject(
        e.graph._project.stringify,
        ProjectUpdate("graphs", "patch", e.patch, Some(e.graph._id.stringify)))
  }
}

object ProjectRoomEventDispatcher extends StartableActor[ProjectRoomEventDispatcher] {
  val name = "projectRoomEventDispatcher"
}