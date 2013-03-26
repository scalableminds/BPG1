import play.api.GlobalSettings
import play.api.Application
import projectZoom.connector.dropbox.DropboxConnector
import projectZoom.artifact.ArtifactActor
import projectZoom.knowledge.KnowledgeActor
import projectZoom.event.EventActor

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    EventActor.start(app)
    ArtifactActor.start(app)
    KnowledgeActor.start(app)
    //DropboxConnector.startAggregating(app)
  }

  override def onStop(app: Application) {
 
  }
}
