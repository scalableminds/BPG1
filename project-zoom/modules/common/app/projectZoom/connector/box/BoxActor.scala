package projectZoom.connector.box

import projectZoom.connector._
import akka.actor._
import scala.concurrent.duration._
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import akka.pattern._
import akka.util.Timeout



class BoxActor(appKeys: BoxAppKeyPair, accessTokens: BoxAccessTokens) extends ArtifactAggregatorActor {
  val TICKER_INTERVAL = 1 minute
  
  implicit val timeout = Timeout(30 seconds)
  
  lazy val tokenActor = context.actorOf(Props(new BoxTokenActor(appKeys, accessTokens)))
  lazy val box = new BoxAPI(appKeys)
  lazy val boxAM = new BoxArtifactMapper(box)
  
  var updateTicker: Cancellable = null
  
  def getArtifacts(implicit accessTokens: BoxAccessTokens) {
    boxAM.getArtifacts.onSuccess{
      case l => l.foreach{ p =>
        val (fileStream, artifactInfo) = p
        fileStream.onSuccess{ 
          case s => publishFoundArtifact(s, p._2)       
        }
      }
    }
  }
  
  def aggregate() = {
    (tokenActor ? AccessTokensRequest).mapTo[Option[BoxAccessTokens]].map{tokenOpt => 
      tokenOpt.map{ implicit token => 
        context.parent ! UpdateBoxAccessTokens(token)
      }
    }
  }
  
  def start = {
    Logger.debug("Starting update ticker")
    updateTicker = context.system.scheduler.schedule(0 seconds, TICKER_INTERVAL, self, Aggregate)
  }

  def stop = {
    updateTicker.cancel
  }
  
  override def preStart(){
    self ! StartAggregating
  }
}