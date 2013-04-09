package models

case class Project()

object Project extends MongoJson {
  override def collection = db("projects")
}