import play.api.GlobalSettings
import play.api.Application
import projectZoom.connector.dropbox.DropboxConnector
import projectZoom.core.artifact.ArtifactActor
import projectZoom.core.knowledge.KnowledgeActor
import projectZoom.core.event.EventActor

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    implicit val application = app
    EventActor.start
    ArtifactActor.start    
    KnowledgeActor.start
    //DropboxConnector.startAggregating(app)
  }

  override def onStop(app: Application) {
 
  }
}
