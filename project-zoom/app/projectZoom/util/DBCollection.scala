package projectZoom.util

import play.modules.reactivemongo._
import scala.concurrent.ExecutionContext

trait DBCollection extends MongoHelpers{
  def collectionName: String
  
  lazy val collection = 
    ReactiveMongoPlugin.db(play.api.Play.current)(collectionName)
}