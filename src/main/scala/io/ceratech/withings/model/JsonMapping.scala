package io.ceratech.withings.model

import play.api.libs.json.{Json, Reads}

/**
  * JSON mapping for the model classes
  *
  * @author dries
  */
trait JsonMapping {

  implicit val measurementReads: Reads[Measurement] = Json.reads[Measurement]
  implicit val measurementGroupReads: Reads[MeasurementGroup] = Json.reads[MeasurementGroup]
  implicit val measurementResponseReads: Reads[MeasurementResponse] = Json.reads[MeasurementResponse]

  implicit def withingResponseReads[T](implicit bodyReads: Reads[T]): Reads[WithingsResponse[T]] = Json.reads[WithingsResponse[T]]
}
