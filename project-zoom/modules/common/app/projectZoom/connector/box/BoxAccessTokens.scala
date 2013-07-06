package projectZoom.connector.box

import play.api.libs.json._

case class BoxAccessTokens(access_token: String, access_token_expires: Long, token_type: String, refresh_token: String, refresh_token_expires: Long)

object BoxAccessTokens extends Function5[String, Long, String, String, Long, BoxAccessTokens]{
  
  val refresh_token_validity = 3600 * 24 * 14 //14 days
  
  implicit val boxAccessTokensFormat = Json.format[BoxAccessTokens]
  implicit val boxExpirationReader = 
    (__).json.update(
        ( __ \ 'access_token_expires).json.copyFrom((__ \ 'expires_in).json.pick[JsNumber].map{
          case JsNumber(t) => JsNumber(System.currentTimeMillis / 1000 + t)
        }
    )) andThen (__).json.update(
        (__ \ 'refresh_token_expires).json.put(
        JsNumber(System.currentTimeMillis() / 1000 + refresh_token_validity)
    )) andThen (__ \ 'expires_in).json.prune
      
}