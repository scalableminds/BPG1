package models

case class Project()

object ProjectDAO extends MongoJsonDAO {
  override def collection = db("projects")
}