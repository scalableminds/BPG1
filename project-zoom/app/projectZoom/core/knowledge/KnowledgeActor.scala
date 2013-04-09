package projectZoom.core.knowledge

import akka.actor.Actor
import java.io.File
import play.api.libs.json.JsValue
import projectZoom.util.DBCollection
import java.io.InputStream
import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api.Logger
import projectZoom.core.event._
import projectZoom.util.StartableActor

class KnowledgeActor extends EventSubscriber with EventPublisher {

  def receive = {
    //case UpdateFileArtifact(info, path, file, metadata) =>
    case x =>
      Logger.debug("Knowledge Actor received: " + x.toString)
  }
  
}

object KnowledgeActor extends StartableActor[KnowledgeActor]{
  def name = "knowledgeActor"
}