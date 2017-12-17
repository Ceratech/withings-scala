package io.ceratech.withings.model

import play.api.libs.json.{Json, OFormat, Reads}

/**
  * JSON mapping for the model classes
  *
  * @author dries
  */
trait JsonMapping {

  implicit val measurementFormat: OFormat[Measurement] = Json.format[Measurement]
  implicit val measurementGrpFormat: OFormat[MeasurementGrp] = Json.format[MeasurementGrp]
  implicit val measurementResponseReads: Reads[MeasurementResponse] = Json.reads[MeasurementResponse]
  implicit val notificationProfileReads: Reads[NotificationProfile] = Json.reads[NotificationProfile]
  implicit val notificationProfilesReads: Reads[NotificationProfiles] = Json.reads[NotificationProfiles]

  implicit def withingResponseReads[T](implicit bodyReads: Reads[T]): Reads[WithingsResponse[T]] = Json.reads[WithingsResponse[T]]
}
