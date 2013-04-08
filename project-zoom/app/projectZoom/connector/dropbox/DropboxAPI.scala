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
import play.api.libs.json._
import projectZoom.core.artifact._
import java.io.IOException
import play.api.Logger
import com.dropbox.client2.DropboxAPI.DropboxInputStream
import java.io.InputStream

case class DropboxUpdates(entries: List[DeltaEntry[Entry]], lastCursor: String) {
  def ++(u: DropboxUpdates) = {
    this.copy(
      entries = entries ::: u.entries,
      lastCursor = u.lastCursor)
  }
}

class DropboxAPI(appKey: AppKeyPair) extends PlayActorSystem with DropboxUpdateHandler {

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

  def withApi[T](f: DropboxAPI.DApi => T): Future[T] = {
    try {
      api.future(timeout).map(f)
    } catch {
      case e: DropboxUnlinkedException =>
        val refreshedApi = createDropboxApi(true)
        api.send(createDropboxApi(true))
        Future.successful(f(refreshedApi))
    }
  }

  private def fetchUpdateList(api: DropboxAPI.DApi, cursor: String): DropboxUpdates = {
    val deltaPage = api.delta(cursor)
    val update = DropboxUpdates(deltaPage.entries.toList, deltaPage.cursor)
    if (deltaPage.hasMore) {
      update ++ fetchUpdateList(api, deltaPage.cursor)
    } else {
      update
    }
  }

  def retrieveFile(path: String, fileHandler: InputStream => ArtifactUpdate) = withApi { api =>
    try {
      Some(fileHandler(api.getFileStream(path, null)))
    } catch {
      case e: Exception =>
        Logger.error("Something went wrong: " + e);
        None
    }
  }

  def fetchUpdates = withApi { api =>
    val updates = fetchUpdateList(api, DropboxAPI.lastCursor)
    DropboxAPI.saveCursor(updates.lastCursor)
    updates.entries
  }

  def updateLocalDropbox: Future[List[Future[Option[Any]]]] = {
    Logger.debug("about to update dropbox")
    DropboxAPI.projects match {
      case Some(projects) =>
        Logger.debug("about to fetch updates")
        fetchUpdates.map { updates =>
          Logger.debug("got updates: " + updates.size)
          val f = updates.flatMap(handleDropboxUpdate(projects))
          Logger.error("Finished handling updates") 
          f
        }
      case _ =>
        Future.successful(Nil)
    }
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

  /*def list(path: String): Future[List[Entry]] = {
    val pretifiedPath = if (path.startsWith("/")) path else "/" + path
    withApi { api =>
      api.metadata(pretifiedPath, 0, null, true, null).contents.asScala.toList
    }
  }*/

  //def accountInfo = withApi(_.accountInfo())
}

case class ProjectRelativePath(projectName: String, path: String)

trait DropboxUpdateHandler {
  import play.api.libs.concurrent.Execution.Implicits._

  val projectRx = "^/([^/]+)/(.*)$".r

  def retrieveFile(path: String, f: InputStream => ArtifactUpdate): Future[Option[ArtifactUpdate]]

  def updateInfo(projectName: String) =
    UpdateInfo(DropboxAPI.identifier, projectName)

  def extractRelativePath(projects: JsValue, update: DeltaEntry[Entry]) = {
    Logger.debug("Update Path: " + update.lcPath)
    update.lcPath match {
      case projectRx(projectPath, path) =>
        (projects \ projectPath).asOpt[String].map { project =>
          ProjectRelativePath(project, path)
        }
      case _ =>
        None
    }
  }

  def metadata2JsValue(dpxEntry: Entry) = {
    Json.obj(
      "fileName" -> dpxEntry.fileName(),
      "hash" -> dpxEntry.hash,
      "mimeType" -> dpxEntry.mimeType,
      "byteSize" -> dpxEntry.bytes,
      "timestamp" -> dpxEntry.clientMtime)
  }

  def handleDropboxUpdate(projects: JsObject)(update: DeltaEntry[Entry]): Option[Future[Option[Any]]] = {
    Logger.debug("about to handle updates")
    extractRelativePath(projects, update).map { relativePath =>
      Logger.debug(s"Relative path: $relativePath")
      Option(update.metadata) match {
        case Some(m) if m.isDir =>
          handleDirUpdate(relativePath, update)
        case Some(m) if !m.isDir =>
          handleFileUpdate(relativePath, update)
        case _ =>
          handleDelete(relativePath, update)
      }
    }
  }

  private def handleDirUpdate(relativePath: ProjectRelativePath, update: DeltaEntry[Entry]): Future[Option[Any]] = {
    Future.successful(None)
  }

  private def handleFileUpdate(relativePath: ProjectRelativePath, update: DeltaEntry[Entry]): Future[Option[Any]] = {
    retrieveFile(update.lcPath, { fileStream =>
      Logger.warn("received file stream!")
      UpdateFileArtifact(
        updateInfo(relativePath.projectName),
        relativePath.path,
        fileStream,
        metadata2JsValue(update.metadata))
    })
  }

  private def handleDelete(relativePath: ProjectRelativePath, update: DeltaEntry[Entry]): Future[Option[Any]] = {
    Future.successful(Some(DeleteFileArtifact(
      updateInfo(relativePath.projectName),
      relativePath.path)))
  }
}

object DropboxAPI extends PlayConfig with ConnectorSettings with DropboxSettings {
  type DApi = com.dropbox.client2.DropboxAPI[WebAuthSession]

  val identifier = "dropbox"

  val accessType = Session.AccessType.DROPBOX

  def create = {
    val settings = awaitSettings
    for {
      key <- (settings \ APP_KEY).asOpt[String]
      secret <- (settings \ APP_SECRET).asOpt[String]
    } yield {
      new DropboxAPI(new AppKeyPair(key, secret))
    }
  }

  def storeToken(token: AccessTokenPair) = {
    storeSetting(ACCESS_TOKEN_KEY, token.key)
    storeSetting(ACCESS_TOKEN_SECRET, token.secret)
    token
  }

  def retrieveNewToken(appKey: AppKeyPair) = {
    storeToken(linkAccount(appKey))
  }

  def loadToken: Option[AccessTokenPair] = {
    val settings = awaitSettings
    for {
      key <- (settings \ ACCESS_TOKEN_KEY).asOpt[String]
      secret <- (settings \ ACCESS_TOKEN_SECRET).asOpt[String]
    } yield {
      new AccessTokenPair(key, secret)
    }
  }

  def projects: Option[JsObject] = {
    (awaitSettings \ PROJECTS).asOpt[JsObject]
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
    (awaitSettings \ CURSOR).asOpt[String] getOrElse null
  }

  def saveCursor(cursor: String) = {
    storeSetting(CURSOR, cursor)
  }
}

trait DropboxSettings {
  val APP_KEY = "appKey"
  val APP_SECRET = "appSecret"
  val ACCESS_TOKEN_KEY = "accessTokenKey"
  val ACCESS_TOKEN_SECRET = "accessTokenSecret"
  val CURSOR = "cursor"
  val PROJECTS = "projects"
}