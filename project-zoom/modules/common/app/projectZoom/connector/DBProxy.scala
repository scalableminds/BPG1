package projectZoom.connector

import models.PermanentValueService
import models.ProjectDAO
import models.GlobalDBAccess
import play.api.libs.json._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import box.BoxAccessTokens
import play.api.Logger


object DBProxy {
  implicit val ctx = models.GlobalAccessContext
  
  def getProjects = ProjectDAO.findAll
  
  def getBoxTokens(): Future[Option[BoxAccessTokens]] = PermanentValueService.get("box.tokens").map{jsonOpt => 
    jsonOpt match  {
      case Some(json) => json.validate[BoxAccessTokens] match {
        case JsSuccess(tokens, _) => Some(tokens)
        case JsError(err) => Logger.error(err.mkString)
          None
      }
      case None => None
    }
  }
  
  def setBoxToken(tokens: BoxAccessTokens) = PermanentValueService.put("box.tokens", BoxAccessTokens.boxAccessTokensFormat.writes(tokens))

  def getBoxEventStreamPos(): Future[Option[Long]] = PermanentValueService.get("box.eventStreamPos").map{jsonOpt =>
    jsonOpt.flatMap(jsNumber => jsNumber.asOpt[Long])
    }
 
  def setBoxEventStreamPos(eventStreamPos: Long) = PermanentValueService.put("box.eventStreamPos", JsNumber(eventStreamPos)) 

}
