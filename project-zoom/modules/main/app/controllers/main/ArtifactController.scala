package controllers.main
import models.ArtifactDAO
import models.ArtifactDAO._
import models.ProjectDAO._
import play.api.libs.json.Json
import projectZoom.util.PlayActorSystem
import projectZoom.core.artifact.ArtifactActor
import projectZoom.core.artifact.RequestResource
import models.ResourceInfo
import models.ArtifactDAO._
import play.api.libs.concurrent.Execution.Implicits._
import models.ProjectDAO
import projectZoom.util.PlayConfig
import projectZoom.util.ExtendedTypes.ExtendedFuture
import akka.util.Timeout
import scala.concurrent.duration._
import java.io.InputStream
import play.api.libs.iteratee.Enumerator
import akka.pattern.AskTimeoutException
import akka.pattern.ask
import scala.concurrent.Future
import models.Implicits._
import controllers.common.ControllerBase
import play.api.i18n.Messages
import models.ResourceLike

object ArtifactController extends ControllerBase with JsonCRUDController with PlayActorSystem with PlayConfig {

  lazy val artifactActor = userActorFor(ArtifactActor.name)

  implicit val timeout = Timeout(config.getInt("artifact.timeout").getOrElse(5) seconds)

  val dao = ArtifactDAO

  def listForProject(projectId: String, offset: Int, limit: Int) = SecuredAction(ajaxCall = true) { implicit request =>
    //TODO: restrict access
    Async {
      for{
        projectOpt <- ProjectDAO.findOneById(projectId).map(_.flatMap(ProjectDAO.asObjectOpt))
        project <- projectOpt ?~ Messages("project.notFound")
      } yield {
        dao.findSomeForProject(project.name, offset, limit).map { l =>
          Ok(withPortionInfo(Json.toJson(l.map(createSingleResult)), offset, limit))
        }
      }
    }
  }

  def download(artifactId: String, name: String, typ: String) = SecuredAction(ajaxCall = true) { implicit request =>
    Async {
      val requestedResource = ResourceInfo(name, typ)
      for {
        artifactOpt <- ArtifactDAO.findOneById(artifactId).map(_.flatMap(ArtifactDAO.asObjectOpt))
        artifact <- artifactOpt ?~ Messages("artifact.notFound")
      } yield {
        artifact
          .resources
          .find(_.isSameAs(requestedResource)) match {
            case Some(resource) =>
              (artifactActor ? RequestResource(artifact, resource))
              .mapTo[Option[InputStream]]
              .map {
                case Some(stream) =>
                  Ok.stream(Enumerator.fromStream(stream))
                case _ =>
                  NotFound
              }
              .recover {
                case a: AskTimeoutException =>
                  NotFound
              }
            case _ =>
              Future.successful(NotFound)
          }
      }
    }
  }
}