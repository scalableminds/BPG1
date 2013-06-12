package projectZoom.connector

import org.joda.time.DateTime
import models.ProjectLike
import akka.agent._
import projectZoom.util.PlayActorSystem
import play.Logger

object FileProjectMatcher extends PlayActorSystem {

  val splittedProjectNames = Agent[Map[String, List[String]]](Map())

  val threshold = 0.5

  private def emailMatching(weight: Double, collaboratorEMails: Set[String], project: ProjectLike) = {
    val stepSize = weight / project.emails.size
    val intersection = (project.emails intersect collaboratorEMails).size
    val total = stepSize * intersection - (stepSize * List(collaboratorEMails.diff(project.emails).size - 5, 0).max)
    List(total, 0).max
  }

  private def matchHeuristic(path: String, collaboratorEMails: Set[String], project: ProjectLike) = {
    val emailValue = emailMatching(0.7, collaboratorEMails, project)
    val pathValue = evaluatePath(0.3, path, project)
    (emailValue -> pathValue)
  }

  def apply(path: String, collaboratorEMails: Set[String], creationDate: DateTime): Option[ProjectLike] = {
    val projects = ProjectCache.getAllProjectsExistingBy(creationDate)
    val evaluatedProjects = projects.zip(projects.map { project =>
      matchHeuristic(path, collaboratorEMails, project)
    })
    val candidates = evaluatedProjects.sortBy(t => t._2._1 + t._2._2).reverse.take(5)
    candidates.head match {
      case (project, t) if (t._1 + t._2 < threshold) =>
        Logger.debug(s"can't match file: ${path}\ncandidates are:")
        candidates.foreach(p=> Logger.debug(s"project ${p._1.name}: email(${p._2._1}) path(${p._2._2})"))
        None
      case (project, t) => 
        Some(project)
      case _ => 
        None
    }
  }

  private def splitAndCanonicalizePath(path: String, separator: String = "/") =
    path.replaceAll("""[-.()=_{}!@?:;"']""", " ").toLowerCase.split(separator).filterNot(_.isEmpty).map(_.split(" ").filterNot(_.isEmpty).toSet)

  private def evaluatePath(weight: Double, path: String, project: ProjectLike): Double = {
    val canonicalPathSegments = splitAndCanonicalizePath(path)
    val projectNameSplit = project.canonicalName.split(" ").filterNot(_.isEmpty).toSet
    val stepSize = weight / projectNameSplit.size
    val maxMatch = canonicalPathSegments.map { seg =>
      (projectNameSplit intersect seg).size
    }.max
    if (maxMatch * stepSize > weight) {
      Logger.debug(s"project: ${project.name}\n")
      Logger.debug(canonicalPathSegments.mkString("\n"))
      Logger.debug(projectNameSplit.mkString(", "))
      Logger.debug(stepSize.toString)
      Logger.debug(maxMatch.toString)
      Logger.debug("")
    }
    stepSize * maxMatch
  }
}