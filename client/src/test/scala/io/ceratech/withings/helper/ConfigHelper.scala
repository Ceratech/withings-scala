package io.ceratech.withings.helper

import com.github.scribejava.core.httpclient.HttpClient
import com.github.scribejava.core.model.OAuthConfig

/**
  * Construct OAuth configs
  *
  * @author dries
  */
object ConfigHelper {

  /**
    * Construct a OAuth config
    *
    * @param client the [[HttpClient]] instance to use
    * @return OAuth config
    */
  def constructConfig(client: HttpClient): OAuthConfig = {
    new OAuthConfig(
      "key",
      "secret",
      "callback",
      null,
      null,
      null,
      null,
      null,
      null,
      client
    )
  }
}
