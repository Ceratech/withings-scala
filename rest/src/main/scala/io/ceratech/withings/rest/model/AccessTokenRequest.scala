package io.ceratech.withings.rest.model

/**
  * Send to the request access token call
  *
  * @author dries
  */
case class AccessTokenRequest(tempToken: String, tempTokenSecret: String, verifier: String)
