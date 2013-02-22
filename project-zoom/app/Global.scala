import play.api.GlobalSettings
import play.api.Application
import projectZoom.connector.dropbox.DropboxConnector
import projectZoom.artifact.ArtifactActor

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    ArtifactActor.start(app)
    //DropboxConnector.startAggregating(app)
  }

  override def onStop(app: Application) {
 
  }
}
