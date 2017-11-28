package io.ceratech.withings.model

/**
  * Base response wrapper, contains status and optional results
  */
case class WithingsResponse[T](status: Int, body: Option[T])

/**
  * Response details for measurements call
  *
  * @param measuregrps the groups contained in the response
  */
case class MeasurementResponse(measuregrps: Seq[MeasurementGroup])

/**
  * Group of measurements, taken in a single session
  *
  * @param grpid    the id of the group
  * @param attrib   the way the measurement was assigned to the user
  * @param date     epoch timestamp
  * @param category 1 for measurements, 2 for goals
  * @param measures the measurements contained in the group
  */
case class MeasurementGroup(grpid: Long, attrib: Int, date: Long, category: Int, measures: Seq[Measurement])

/**
  * Single measurement
  *
  * @param value  the value
  * @param unit   the power of 10 to multiply value with
  * @param `type` the type of measurement
  */
case class Measurement(value: Int, unit: Int, `type`: Int)