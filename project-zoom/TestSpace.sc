object TestSpace {
  import models.Project
  import models.ProjectDAO
  import play.api.libs.json.Json

  val js = Json.obj(
    "_id" -> "517188503A029499DDE7A286",
    "firstName" -> "T",
    "lastName" -> "T",
    "email" -> "tombocklisch@gmail.com")
    
  ProjectDAO.reads(js)
}