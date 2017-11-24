package io.ceratech.withings

import com.github.scribejava.core.builder.api.DefaultApi10a
import com.github.scribejava.core.model.OAuth1RequestToken

/**
  * Withings OAuth API definition
  *
  * @author dries
  */
object WithingsApi extends DefaultApi10a {

  override def getAccessTokenEndpoint: String = "https://developer.health.nokia.com/account/access_token"

  override def getRequestTokenEndpoint: String = "https://developer.health.nokia.com/account/request_token"

  override def getAuthorizationUrl(requestToken: OAuth1RequestToken): String =
    s"https://developer.health.nokia.com/account/authorize?token=${requestToken.getToken}"
}
