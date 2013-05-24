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

object Global extends GlobalSettings with GlobalDBAccess {

  override def onStart(app: Application) {
    implicit val sys = Akka.system(app)
    EventActor.start
    SettingsActor.start
    ArtifactActor.start
    KnowledgeActor.start
    TextThumbnailActor.start
    SupervisorActor.start
    if (app.mode == Mode.Dev)
      putSampleValuesInDB()
  }

  def putSampleValuesInDB() = {
    GraphDAO.findOne.map {
      case None =>
        val nodes = List(
          Node(1, Position(10, 10), "project", NodePayload("Service Experience - B")),
          Node(2, Position(50, 50), "project", NodePayload("Library Experience - B")))
        val edges = List(models.Edge(1, 2, Some("Penis")))
        val clusters = List()

        val g = models.Graph("Test", 1, 1, nodes, edges, clusters)
        GraphDAO.insert(g)
      case _ =>
    }
  }

  override def onStop(app: Application) {

  }
}