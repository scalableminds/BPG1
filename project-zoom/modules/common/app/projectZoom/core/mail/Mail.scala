package projectZoom.core.mail

case class Mail(
    from: String = "",
    subject: String = "",
    bodyText: String = "",
    bodyHtml: String = "",
    recipients: List[String] = List(),
    ccRecipients: List[String] = List(),
    bccRecipients: List[String] = List(),
    replyTo: Option[String] = None,
    headers: Map[String, String] = Map[String, String]() )