package io.ceratech.withings

import com.github.scribejava.core.model.OAuth1AccessToken
import io.swagger.annotations.ApiModelProperty

import scala.annotation.meta.field

/**
  * Withings access token; use to make requests to the API
  *
  * @author dries
  */
case class WithingsAccessToken(token: String, tokenSecret: String) {

  @(ApiModelProperty @field)(hidden = true)
  lazy val oauthAccessToken: OAuth1AccessToken = new OAuth1AccessToken(token, tokenSecret)
}
