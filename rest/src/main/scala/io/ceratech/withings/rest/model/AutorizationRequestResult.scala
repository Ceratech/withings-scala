package io.ceratech.withings.rest.model

/**
  * Result of the authorization request call
  *
  * @author dries
  */
case class AutorizationRequestResult(url: String, tempToken: String, tempTokenSecret: String)
