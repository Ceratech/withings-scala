package io.ceratech.withings.rest.model

import java.time.ZonedDateTime

/**
  * Parameters for the get measurements call
  *
  * @author dries
  */
case class MeasurementsParameters(userId: Long, startDate: ZonedDateTime, endDate: ZonedDateTime)