package io.ceratech.withings.rest.model

/**
  * Result of the authorization request call
  *
  * @author dries
  */
case class AuthorizationRequestResult(url: String, tempToken: String, tempTokenSecret: String)
