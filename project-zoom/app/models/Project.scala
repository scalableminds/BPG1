package models

import reactivemongo.bson.BSONObjectID
import projectZoom.core.security.Permission

case class Participant(duty: String, _user: String)

case class Project(name: String, picUrl: String, _tags: List[BSONObjectID], participants: List[Participant], _graphs: List[BSONObjectID], _id: BSONObjectID = BSONObjectID.generate)

object ProjectDAO extends MongoJsonDAO {
  override val collectionName = "projects"
}