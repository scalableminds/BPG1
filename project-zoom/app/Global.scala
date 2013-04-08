import play.api.GlobalSettings
import play.api.Application
import projectZoom.connector.dropbox.DropboxConnector
import projectZoom.core.artifact.ArtifactActor
import projectZoom.core.knowledge.KnowledgeActor
import projectZoom.core.event.EventActor
import projectZoom.thumbnails.TextThumbnailActor
import play.api.libs.concurrent.Akka

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    implicit val sys = Akka.system(app)
    EventActor.start
    ArtifactActor.start    
    KnowledgeActor.start
    TextThumbnailActor.start
    //DropboxConnector.startAggregating(app)
  }

  override def onStop(app: Application) {
 
  }
}
