import play.api.GlobalSettings
import play.api.Application
import projectZoom.connector.dropbox.DropboxConnector
import projectZoom.core.artifact.ArtifactActor
import projectZoom.core.knowledge.KnowledgeActor
import projectZoom.core.event.EventActor
import projectZoom.thumbnails.text.TextThumbnailActor
import projectZoom.connector.SupervisorActor
import play.api.libs.concurrent.Akka
import projectZoom.core.settings.SettingsActor
import play.api.Mode
import models.Profile
import play.api.Logger
import models.Node
import models.Position
import models.NodePayload
import models.Cluster
import models.GraphDAO
import models.GraphDAO._
import models.GlobalDBAccess
import play.api.libs.concurrent.Execution.Implicits._
import java.util.UUID
import akka.actor.ActorSystem
import scala.concurrent.duration._
import play.api.libs.json.JsNull
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import java.io.FileInputStream
import java.io.File
import models.ProjectDAO
import projectZoom.thumbnails.all.AllThumbnailActor
import projectZoom.thumbnails.image.ImageThumbnailActor
import projectZoom.thumbnails.video.VideoThumbnailActor

object Global extends GlobalSettings with GlobalDBAccess {

  override def onStart(app: Application) {
    implicit val sys = Akka.system(app)
    EventActor.start
    SettingsActor.start
    val aa = ArtifactActor.start
    KnowledgeActor.start
    TextThumbnailActor.start
    AllThumbnailActor.start
    ImageThumbnailActor.start
    VideoThumbnailActor.start
    SupervisorActor.start
    if (app.mode == Mode.Dev) {
      putSampleValuesInDB

      sys.scheduler.scheduleOnce(5 seconds) {
        //ProjectDAO.findOneByName(_project)
        List(
            models.Artifact("test.jpg", "null - null", "prototype", "dummy", Json.obj()) ->
              new FileInputStream(new File("public/images/test.jpg"))
        ).map{
          case (info, stream) =>
            Logger.debug("Inserted dummy Artifact: " + info)
            aa ! projectZoom.core.artifact.ArtifactFound(stream, info)
        }
      }
    }
  }

  def putSampleValuesInDB(implicit sys: ActorSystem) = {

  }

  override def onStop(app: Application) {

  }
}