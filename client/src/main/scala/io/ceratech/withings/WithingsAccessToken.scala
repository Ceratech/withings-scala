package io.ceratech.withings

import com.github.scribejava.core.model.OAuth1AccessToken

/**
  * Withings access token; use to make requests to the API
  *
  * @author dries
  */
case class WithingsAccessToken(token: String, tokenSecret: String) {

  lazy val oauthAccessToken: OAuth1AccessToken = new OAuth1AccessToken(token, tokenSecret)
}
