package projectZoom.connector

import org.joda.time.DateTime
import models.ProjectLike
import akka.agent._
import akka.actor._
import projectZoom.util.PlayActorSystem
import play.Logger
import scala.concurrent._
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import projectZoom.connector.util.{Jaccard, Levenshtein}

case class BoxFileInfo(name: String, path: String, collaborators: Set[String])

class FileProjectMatcher extends Actor {
  
  Logger.debug(s"FileProjectMatcherPath: ${context.self.path.toString}")
  
  implicit val timeout = Timeout(60 seconds)
  
  val projectCache = context.actorFor(s"${context.parent.path}/ProjectCache")
  def projects = (projectCache ? ProjectsRequest).mapTo[List[ProjectLike]]
  
  val collaboratorWeight = 0.6
  val pathWeight = 0.4
  val threshold = 0.5

  def receive = {
    case BoxFileInfo(name, path, collaborators) => 
      val cachedSender = sender
      matchHeuristic(path, collaborators).foreach{ projectOpt => 
        Logger.debug(s"Found project $projectOpt")
        cachedSender ! projectOpt}
  }
  
  def matchBoxFile(name: String, path: String, collaborators: Set[String]): Future[Option[ProjectLike]] = {
    projects.map(projects => projects.headOption)
  }
  
  def emailMatching(collaborators: Set[String], participants: Set[String]) = {
     Jaccard(collaborators, participants)
  }
  
  def pathMatching(pathSplit: List[String], projectName: String): Double = {
    pathSplit.map(pathSegment => Levenshtein.similarity(pathSegment, projectName)).max
  }
  
  def evaluate(pathSplit: List[String], collaborators: Set[String], project: ProjectLike ) = {
    (emailMatching(collaborators, project.emails) * collaboratorWeight ->
    pathMatching(pathSplit, project.canonicalName) * pathWeight)
  }
  
  def matchHeuristic(path: String, collaborators: Set[String]) = {
    Logger.debug(s"match heuristic on path $path")
    val pathSplit = splitAndCanonicalizePath(path)
    projects.map{ projects => 
      val evaluatedProjects = projects.zip(projects.map(project => evaluate(pathSplit, collaborators, project)))
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
  }
  
  private def splitAndCanonicalizePath(path: String, separator: String = "/") =
    path.replaceAll("""[-.()=_{}!@?:;"']""", " ").toLowerCase.split(separator).filterNot(_.isEmpty).toList
}