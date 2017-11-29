package io.ceratech.withings.rest.model

/**
  * Generic request to the Withings client
  *
  * @author dries
  */
case class WithingsClientRequest[T](token: String, tokenSecret: String, parameters: T)
