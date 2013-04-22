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
import models.User
import models.Profile
import play.api.Logger
import models.ProfileDAO

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    implicit val sys = Akka.system(app)
    EventActor.start
    SettingsActor.start
    ArtifactActor.start    
    KnowledgeActor.start
    TextThumbnailActor.start
    SupervisorActor.start
    if(app.mode == Mode.Dev)
      putSampleValuesInDB()
  }
  
  def putSampleValuesInDB() = {
    //User.insert()
  }

  override def onStop(app: Application) {
    
  }
}
