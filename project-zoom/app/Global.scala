import play.api.GlobalSettings
import play.api.Application
import projectZoom.connector.dropbox.DropboxConnector

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    DropboxConnector.startAggregating
  }

  override def onStop(app: Application) {

  }
}
