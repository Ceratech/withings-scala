package io.ceratech.withings.rest.model

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import io.ceratech.withings.WithingsAccessToken
import io.ceratech.withings.model.{JsonMapping, MeasurementGroup, RegisteredNotification}
import play.api.libs.json.{Json, OFormat, Reads, Writes}

/**
  * JSON mappings
  *
  * @author dries
  */
trait RestJsonMapping extends JsonMapping {
  implicit val zonedDateTimeWrites: Writes[ZonedDateTime] = Writes.DefaultZonedDateTimeWrites
  implicit val zonedDateTimeReads: Reads[ZonedDateTime] = Reads.DefaultZonedDateTimeReads

  implicit val authorizationRequestResultWrites: Writes[AuthorizationRequestResult] = Json.writes[AuthorizationRequestResult]
  implicit val accessTokenRequestReads: Reads[AccessTokenRequest] = Json.reads[AccessTokenRequest]
  implicit val withingsAccessTokenFormat: OFormat[WithingsAccessToken] = Json.format[WithingsAccessToken]
  implicit val registerNotificationParametersReads: Reads[RegisterNotificationParameters] = Json.reads[RegisterNotificationParameters]
  implicit val measurementsParametersReads: Reads[MeasurementsParameters] = Json.reads[MeasurementsParameters]
  implicit val measurementsGroupWrites: Writes[MeasurementGroup] = Json.writes[MeasurementGroup]
  implicit val registeredNotificationWrites: Writes[RegisteredNotification] = Json.writes[RegisteredNotification]

  implicit def withingsClientRequestReads[T](implicit bodyReads: Reads[T]): Reads[WithingsClientRequest[T]] = Json.reads[WithingsClientRequest[T]]
}
