package models

import reactivemongo.bson.BSONObjectID
import projectZoom.core.security.Permission
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import play.modules.reactivemongo.json.BSONFormats._
import scala.concurrent.Future
import reactivemongo.core.commands.LastError
import play.api.Logger
import org.joda.time.DateTime

case class ProjectLike(name: String, participants: List[Participant], season: String, year: String, length: String, _tags: List[String]){
  lazy val canonicalName = name.replaceAll("""[-.()=_{}!@?:;"']""", " ").toLowerCase()
  lazy val emails = participants.map(_._user.toLowerCase()).toSet
  lazy val startDate = new DateTime(year.toInt, if(season=="ST") 4 else 10, 1, 0, 0)
}

case class Participant(duty: String, _user: String)

case class Project(name: String, participants: List[Participant], season: String, year: String, length: String, _tags: List[String], _graphs: Option[List[String]], _id: BSONObjectID = BSONObjectID.generate)

object ProjectDAO extends SecuredMongoJsonDAO[Project] {
    /**
   * Name of the DB collection
   */
  override val collectionName = "projects"
    
  override val defaultOrderBy = "name"

  implicit val participantFormat = Json.format[Participant]
  implicit val projectFormat = Json.format[Project]
  implicit val projectLikeFormat = Json.format[ProjectLike]

  def findOneByName(_project: String)(implicit ctx: DBAccessContext) = {
    find("name", _project).one[JsObject]
  }

  def addGraphTo(projectId: String, graph: Graph)(implicit ctx: DBAccessContext) = {
    withId(projectId) { id =>
      collectionUpdate(Json.obj("_id" -> id),
        Json.obj("$push" -> Json.obj("_graphs" -> graph.group)))
    }
  }
  
  def findProject(projectName: String)(implicit ctx: DBAccessContext): Future[Option[Project]] = {
    findOneByName(projectName).map(_.flatMap(_.asOpt[Project]))
  }

  def update(p: Project)(implicit ctx: DBAccessContext): Future[LastError] =
    collectionUpdate(Json.obj("name" -> p.name),
      Json.obj("$set" -> p), upsert = true)

  def update(p: ProjectLike)(implicit ctx: DBAccessContext): Future[LastError] =
    collectionUpdate(Json.obj("name" -> p.name),
      Json.obj("$set" -> p), upsert = true)
}