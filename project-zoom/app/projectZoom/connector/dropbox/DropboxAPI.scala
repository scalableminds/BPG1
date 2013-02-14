package projectZoom.connector.dropbox

import com.dropbox.client2._
import com.dropbox.client2.session._
import com.dropbox.client2.exception._
import com.dropbox.client2.DropboxAPI.Entry
import java.util.Properties
import java.io.{ File, FileOutputStream, FileInputStream }
import java.awt.Desktop
import collection.JavaConverters._
import projectZoom.util.PlayConfig
import projectZoom.connector.ConnectorSettings
import akka.agent.Agent
import projectZoom.util.PlayActorSystem
import scala.concurrent.duration._
import scala.concurrent.Future
import com.dropbox.client2.DropboxAPI.DeltaEntry
import scala.collection.JavaConversions._

case class DropboxUpdates(entries: List[DeltaEntry[Entry]], lastCursor: String) {
  def ++(u: DropboxUpdates) = {
    this.copy(
      entries = entries ::: u.entries,
      lastCursor = u.lastCursor)
  }
}

class DropboxAPI(appKey: AppKeyPair) extends PlayActorSystem{
  type DApi = com.dropbox.client2.DropboxAPI[WebAuthSession]

  lazy val timeout = 1 second

  lazy val api = Agent(createDropboxApi())

  def createDropboxApi(lastTokenWasInvalid: Boolean = false) = {
    val token =
      if (lastTokenWasInvalid)
        DropboxAPI.retrieveNewToken(appKey)
      else
        DropboxAPI.obtainToken(appKey)

    var session = new WebAuthSession(appKey, DropboxAPI.accessType, token)

    new com.dropbox.client2.DropboxAPI(session)
  }

  def withApi[T](f: DApi => T): Future[T] = {
    try {
      api.future(timeout).map(f)
    } catch {
      case e: DropboxUnlinkedException =>
        val refreshedApi = createDropboxApi(true)
        api.send(createDropboxApi(true))
        Future.successful(f(refreshedApi))
    }
  }

  private def fetchUpdateList(api: DApi, cursor: String): DropboxUpdates = {
    val deltaPage = api.delta(cursor)
    val update = DropboxUpdates(deltaPage.entries.toList, deltaPage.cursor)
    if (deltaPage.hasMore) {
      update ++ fetchUpdateList(api, deltaPage.cursor)
    } else {
      update
    }
  }

  def fetchUpdates = withApi { api =>
    val updates = fetchUpdateList(api, DropboxAPI.lastCursor)
    DropboxAPI.saveCursor(updates.lastCursor)
    updates.entries
  }
  
  def updateLocalDropbox = {
    fetchUpdates.map{ updates =>
      updates.map( handleUpdate)
      updates
    }
  }
  
  private def handleUpdate(update: DeltaEntry[Entry]) = {
    
  }

  /*
  def tryOperation[T](ignoreCodes: Int*)(operation: => T): Option[T] = {
    try {
      Some(operation)
    } catch {
      case e: DropboxServerException if ignoreCodes.contains(e.error) => None
    }
  }
*/
  
  def list(path: String): Future[List[Entry]] = {
    val pretifiedPath = if (path.startsWith("/")) path else "/" + path
    withApi { api =>
      api.metadata(pretifiedPath, 0, null, true, null).contents.asScala.toList
    }
  }

  //def accountInfo = withApi(_.accountInfo())
}

object DropboxAPI extends PlayConfig with ConnectorSettings {
  val identifier = "dropbox"

  val accessType = Session.AccessType.DROPBOX

  def create = {
    val settings = awaitSettings
    for {
      key <- (settings \ "appKey").asOpt[String]
      secret <- (settings \ "appSecret").asOpt[String]
    } yield {
      new DropboxAPI(new AppKeyPair(key, secret))
    }
  }

  def storeToken(token: AccessTokenPair) = {
    storeSetting("accessTokenKey", token.key)
    storeSetting("accessTokenSecret", token.secret)
    token
  }

  def retrieveNewToken(appKey: AppKeyPair) = {
    storeToken(linkAccount(appKey))
  }

  def loadToken: Option[AccessTokenPair] = {
    val settings = awaitSettings
    for {
      key <- (settings \ "accessTokenKey").asOpt[String]
      secret <- (settings \ "accessTokenSecret").asOpt[String]
    } yield {
      new AccessTokenPair(key, secret)
    }
  }

  def obtainToken(appKey: AppKeyPair): AccessTokenPair =
    loadToken getOrElse retrieveNewToken(appKey)

  def linkAccount(appKey: AppKeyPair): AccessTokenPair = {
    val was = new WebAuthSession(appKey, accessType);
    val info = was.getAuthInfo
    val url = info.url
    // TODO: Somehow grab the keys automatically
    // ...
    println("Go to: " + url)
    println("Allow access to this app and press ENTER")
    while (System.in.read() != '\n') {}

    // This will fail if the user did not allow the app
    val uid = was.retrieveWebAccessToken(info.requestTokenPair)
    val accessToken = was.getAccessTokenPair
    accessToken
  }

  def lastCursor = {
    (awaitSettings \ "cursor").asOpt[String] getOrElse null
  }

  def saveCursor(cursor: String) = {
    storeSetting("cursor", cursor)
  }
}