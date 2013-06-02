package projectZoom.connector.box

import api._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.Logger

class BoxExtendedAPI(appKeys: BoxAppKeyPair) extends BoxAPI(appKeys) {
  def fetchCollaborators(file: BoxFile)(implicit accessTokens: BoxAccessTokens) = 
    fetchCollaboratorsList(file.parent)
  
  def fetchCollaborators(folder: BoxBaseFolder)(implicit accessTokens: BoxAccessTokens) = 
    fetchCollaboratorsList(folder)
    
  private def fetchCollaboratorsList(folder: BoxBaseFolder)(implicit accessTokens: BoxAccessTokens) = {
    fetchFolderCollaborations(folder.id).map{ json =>
      (json \ "entries").validate[List[BoxCollaboration]] match {
        case JsSuccess(collaborations, _) => collaborations.map(c => c.accessible_by)
        case JsError(err) => Logger.error(s"Error fetching collaborations:\n ${err.mkString}")
        List()
      }
    }
  }
}