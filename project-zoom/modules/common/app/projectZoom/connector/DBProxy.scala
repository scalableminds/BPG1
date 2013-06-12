package projectZoom.connector

import models.PermanentValueService
import models.{ProjectDAO, ProjectLike}
import models.{ArtifactDAO, Artifact, ArtifactTransformers}
import models.GlobalDBAccess
import play.api.libs.json._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import box.BoxAccessTokens
import play.api.Logger

object DBProxy extends ArtifactTransformers{
  implicit val ctx = models.GlobalAccessContext
  import ProjectDAO.projectLikeFormat
  
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

  def deleteBoxEventStreamPos() = PermanentValueService.del("box.eventStreamPos")
  
  def getProjects = ProjectDAO.findAll.map{jsonList =>
    jsonList.flatMap{ json => 
      json.validate[ProjectLike] match {
        case JsSuccess(proj, _) =>  Some(proj)
        case JsError(err) => Logger.error(s"Error reading projects:\n${err.mkString}")
          None
      }
    }
  }
  
  def getArtifactsForSource(source: String) = ArtifactDAO.findAllForSource(source).map{jsonFut => 
    jsonFut.map {json => 
      json.validate[Artifact] match {
        case JsSuccess(artifact, _) => Some(artifact)
        case JsError(err) => Logger.error(s"Error reading artifact:\n${err.mkString}")
          None
      }
    }
  }
  
  
}