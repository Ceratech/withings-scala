package io.ceratech.withings.model

import java.time.ZonedDateTime

/**
  * Base response wrapper, contains status and optional results
  */
case class WithingsResponse[T](status: Int, body: Option[T], error: Option[String])

/**
  * Response details for measurements call
  *
  * @param measuregrps the groups contained in the response
  */
case class MeasurementResponse(timezone: String, measuregrps: Seq[MeasurementGrp])

/**
  * Group of measurements, taken in a single session
  *
  * @param grpid    the id of the group
  * @param attrib   the way the measurement was assigned to the user
  * @param date     epoch timestamp
  * @param category 1 for measurements, 2 for goals
  * @param measures the measurements contained in the group
  */
case class MeasurementGrp(grpid: Long, attrib: Int, date: Long, category: Int, measures: Seq[Measurement])

/**
  * More user-friendly version of the [[MeasurementGrp]]
  * @param groupId the group identifier
  * @param attributed the way this was attributed to the user
  * @param date the date of measuring
  * @param category the
  * @param measures the measures in the group
  */
case class MeasurementGroup(groupId: Long, attributed: Int, date: ZonedDateTime, category: Int, measures: Seq[Measurement])

/**
  * Single measurement
  *
  * @param value  the value
  * @param unit   the power of 10 to multiply value with
  * @param `type` the type of measurement
  */
case class Measurement(value: Int, unit: Int, `type`: Int)