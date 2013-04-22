package models

import reactivemongo.bson.BSONObjectID
import projectZoom.core.security.Permission
import play.api.libs.json.JsObject
import play.api.libs.concurrent.Execution.Implicits._

case class ProjectLike(name: String, participants: List[Participant], tags: List[String])

case class Participant(duty: String, _user: String)

case class Project(name: String, picUrl: String, _tags: List[BSONObjectID], participants: List[Participant], _graphs: List[BSONObjectID], _id: BSONObjectID = BSONObjectID.generate)

object ProjectDAO extends MongoJsonDAO {
  override val collectionName = "projects"
    
  def findOneByName(_project: String) = {
    find("name", _project).one[JsObject]
  }
}