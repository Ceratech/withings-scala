package io.ceratech.withings.rest.model

/**
  * Parameters required to register a callback notification on the API
  *
  * @author dries
  */
case class RegisterNotificationParameters(userId: Long, callback: String, comment: String, application: Int)
