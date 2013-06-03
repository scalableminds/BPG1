package controllers.main
import models.TagDAO
import controllers.common.ControllerBase
import models.DBAccessContext

object TagController extends ControllerBase with JsonCRUDController {
  val dao = TagDAO

  override def singleObjectFinder(name: String)(implicit ctx: DBAccessContext) =
    dao.findByName(name)

}