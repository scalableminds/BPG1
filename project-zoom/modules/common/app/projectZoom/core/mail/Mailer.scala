package projectZoom.core.mail

import akka.actor._
import scala.concurrent.duration._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import play.api.Configuration
import play.api.Play.current

import com.typesafe.plugin._

case class Send(mail: Mail)

class Mailer extends Actor {

  val subjectPrefix = current.configuration.getBoolean("mail.prefix").get

  def receive = {
    case Send(mail) =>
      send(mail)
  }

  /**
   * Sends an email based on the provided data
   */
  def send(mailGauge: Mail) = {
    println(s"about to send mail: $mailGauge")
    
    val mail = use[MailerPlugin].email
    mail.setSubject(subjectPrefix + mailGauge.subject)
    mail.addFrom(mailGauge.from)

    mailGauge.replyTo.map(mail.setReplyTo)
    mail.addRecipient(mailGauge.recipients: _*)
    mail.addBcc(mailGauge.bccRecipients: _*)
    mail.addCc(mailGauge.ccRecipients: _*)

    mailGauge.headers foreach { case (key, value) => mail.addHeader(key, value) }

    mail.send(mailGauge.bodyText, mailGauge.bodyHtml)
    println(s"sent mail")

  }
}