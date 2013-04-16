package models

case class Project()

object ProjectDAO extends MongoJsonDAO {
  override val collectionName = "projects"
}