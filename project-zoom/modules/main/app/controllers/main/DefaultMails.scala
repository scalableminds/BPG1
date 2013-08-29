package controllers.main

import views._

object DefaultMails {
  /**
   * Configuration used for settings
   */
  val conf = play.api.Play.current.configuration

  /**
   * Base url used in emails
   */
  val uri = conf.getString("http.uri") getOrElse ("http://localhost")

  val defaultFrom = "no-reply@projectZoom.de"
  /**
   * Creates a registration mail which should allow the user to verify his
   * account
   *
   * All the emails should be created from templates and called via a method on this object
   */
}