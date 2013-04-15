package controllers

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
   */
  /*def registerAdminNotifyerMail(name: String, brainDBResult: String) =
    Mail(
      from = defaultFrom,
      subject = "A new user (" + name + ") registered on oxalis.at",
      bodyText = html.mail.registerAdminNotify(name, brainDBResult).body,
      recipients = List("braintracing@neuro.mpg.de"))

  def registerMail(name: String, receiver: String, brainDBresult: String) =
    Mail(
      from = defaultFrom,
      subject = "Thanks for your registration on " + uri,
      bodyText = html.mail.register(name, Messages(brainDBresult)).body,
      recipients = List(receiver))

  def verifiedMail(name: String, receiver: String) =
    Mail(
      from = defaultFrom,
      subject = "Your account on " + uri + "got activated",
      bodyText = html.mail.validated(name).body,
      recipients = List(receiver))*/
}