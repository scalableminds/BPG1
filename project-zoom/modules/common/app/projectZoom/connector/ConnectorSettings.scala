package projectZoom.connector

import projectZoom.util._
import reactivemongo.bson._
import play.api.libs.json.Json
import play.api.libs.json.Reads._
import play.modules.reactivemongo._
import play.api.libs.json._
import akka.agent.Agent
import scala.concurrent.Await
import scala.concurrent.duration._
import play.api.Logger
import java.util.concurrent.TimeoutException
import scala.concurrent.Future
import reactivemongo.core.commands.LastError
import models.MongoJsonDAO
import play.api.libs.concurrent.Execution.Implicits._
import models.UnsecuredMongoJsonDAO

trait ConnectorSettings extends UnsecuredMongoJsonDAO[JsValue] {
  def identifier: String

  val MAX_SETTINGS_AWAIT = 1 second

  lazy val settingsQuery = Json.obj("identifier" -> identifier)

  val collectionName = "connector-settings"

  def defaultSettings = Json.obj()

  def awaitSettings: JsValue = {
    try {
      Await.result(
        collectionFind(settingsQuery)
          .one[JsValue].map(_ getOrElse defaultSettings),
        MAX_SETTINGS_AWAIT)
    } catch {
      case e: TimeoutException =>
        Logger.error(s"Failed to load settings for $identifier due to timeout.")
        defaultSettings
    }
  }

  def storeSetting(key: String, value: String): Future[LastError] = {
    storeSetting(key, JsString(value))
  }

  def storeSetting(key: String, value: JsValue) = {
    collectionUpdate(
      settingsQuery,
      Json.obj("$set" -> Json.obj(key -> value)),
      upsert = true)
  }

  def storeSettings(settings: JsObject) = {
    collectionUpdate(
      settingsQuery,
      settings,
      upsert = true)
  }
}