package controllers

import play.api.mvc.Controller
import securesocial.core.SecureSocial
import models.Tag
import models.TagDAO
import play.api.libs.json.Json
import projectZoom.util.PlayActorSystem
import play.api.libs.json.JsObject

object TagController extends ControllerBase with JsonCRUDController{
  val dao = TagDAO
  
}