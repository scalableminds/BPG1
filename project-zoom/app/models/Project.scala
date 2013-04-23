package models

import reactivemongo.bson.BSONObjectID
import projectZoom.core.security.Permission
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import play.modules.reactivemongo.json.BSONFormats._

case class ProjectLike(name: String, participants: List[Participant], season: String, year: String, length: String, _tags: List[String])

case class Participant(duty: String, _user: String)

case class Project(name: String, picUrl: String, _tags: List[String], participants: List[Participant], _graphs: List[BSONObjectID], _id: BSONObjectID = BSONObjectID.generate)

object ProjectDAO extends MongoJsonDAO {
  override val collectionName = "projects"

  implicit val participantFormat = Json.format[Participant]
  implicit val projectFormat = Json.format[Project]
  implicit val projectLikeFormat = Json.format[ProjectLike]

  def findOneByName(_project: String) = {
    find("name", _project).one[JsObject]
  }

  def update(p: Project) =
    collection.update(Json.obj("name" -> p.name),
      Json.obj("$set" -> p), upsert = true)

  def update(p: ProjectLike) =
    collection.update(Json.obj("name" -> p.name),
      Json.obj("$set" -> p), upsert = true)

}