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
import controllers.main.project.ProjectRoomEventDispatcher
import akka.actor.ActorRef

/**
 * Main Entry and configuration point for the application
 */
object Global extends GlobalSettings with GlobalDBAccess {

  /**
   * Called as the first function after application start. Brings all actors to
   * life and inserts initial data.
   */
  override def onStart(app: Application) {
    implicit val sys = Akka.system(app)

    startCoreActors
    startExternalActors

    if (app.mode == Mode.Dev) {
      mockupFoundArtefacts(sys.actorFor("/user/" + ArtifactActor.name))
    }
  }
  
  /**
   * Starts all actors necessary for the backend core
   */
  def startCoreActors(implicit sys: ActorSystem) {
    EventActor.start
    SettingsActor.start
    ArtifactActor.start
  }

  /**
   * Starts all actors used by external components
   */
  def startExternalActors(implicit sys: ActorSystem) {
    KnowledgeActor.start
    TextThumbnailActor.start
    AllThumbnailActor.start
    ImageThumbnailActor.start
    VideoThumbnailActor.start
    ProjectRoomEventDispatcher.start
    SupervisorActor.start
  }
  
  /**
   * Simulates found artifacts. Should be used in development mode to ensure
   * that there is some data to work with
   */
  def mockupFoundArtefacts(artifactActor: ActorRef)(implicit sys: ActorSystem) {
    sys.scheduler.scheduleOnce(5 seconds) {
      new File("modules/common/public/testfiles").listFiles.map { f =>
        models.Artifact(f.getName, "Project-Zoom", "someFolder", "dummy", 0, Json.obj()) ->
          new FileInputStream(f)
      }.map {
        case (info, stream) =>
          Logger.debug("Inserted dummy Artifact: " + info)
          artifactActor ! projectZoom.core.artifact.ArtifactFound(stream, info)
      }
    }
  }

  override def onStop(app: Application) {

  }
}