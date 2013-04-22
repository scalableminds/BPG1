package controllers

import securesocial.core.SecureSocial
import models.ArtifactDAO
import play.api.libs.json.Json
import projectZoom.util.PlayActorSystem
import projectZoom.core.artifact.ArtifactActor
import akka.pattern.ask
import projectZoom.core.artifact.RequestResource
import models.ArtifactInfo
import models.ResourceInfo
import play.api.libs.concurrent.Execution.Implicits._

object ArtifactController extends ControllerBase with JsonCRUDController with PlayActorSystem {

  lazy val artifactActor = userActorFor(ArtifactActor.name)

  val dao = ArtifactDAO

  def index = SecuredAction { implicit request =>
    Ok(views.html.index())
  }

  def list(project: String, offset: Int, limit: Int) = SecuredAction { implicit request =>
    //TODO: restrict access
    Async {
      dao.findSomeForProject(project, offset, limit).map { l =>
        Ok(withPortionInfo(Json.toJson(l), offset, limit))
      }
    }
  }

  def download() = SecuredAction { implicit request =>
    Async {
      //val artifactInfo = ArtifactInfo()
      //val resourceInfo = ResourceInfo()
???
      //artifactActor ? RequestResource(artifactInfo, resourceInfo)
    }
  }
}