package projectZoom.connector

import models.PermanentValueService
import models.ProjectDAO
import models.GlobalDBAccess

object DBProxy {
  //def getProjects = ProjectDAO.findAll
  
  def getPermanentValue(key: String) = PermanentValueService.get(key)
}