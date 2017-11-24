package io.ceratech.withings

import com.github.scribejava.core.model.OAuthRequest

/**
  * Implicit helpers
  *
  * @author dries
  */
object Implicits {

  class PimpedOAuthRequest(request: OAuthRequest) {

    def addBodyParameters(parameters: Map[String, Any]): Unit = {
      parameters.mapValues(_.toString).foreach {
        case (key, value) ⇒ request.addBodyParameter(key, value)
      }
    }
  }

  implicit def pimpOAuthRequest(request: OAuthRequest): PimpedOAuthRequest = new PimpedOAuthRequest(request)

}
