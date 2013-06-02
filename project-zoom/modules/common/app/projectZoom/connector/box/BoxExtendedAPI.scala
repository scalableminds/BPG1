package projectZoom.connector.box

import api._

class BoxExtendedAPI(appKeys: BoxAppKeyPair) extends BoxAPI(appKeys) {
  def fetchCollaborators(file: BoxFile)(implicit accessTokens: BoxAccessTokens) = 
    fetchFolderCollaborations(file.parent.id)
  
  def fetchCollaborators(folder: BoxFolder)(implicit accessTokens: BoxAccessTokens) = 
    fetchFolderCollaborations(folder.id)
}