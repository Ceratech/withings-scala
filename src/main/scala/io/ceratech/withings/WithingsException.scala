package io.ceratech.withings

/**
  * API exception
  *
  * @author dries
  */
case class WithingsException(message: String) extends RuntimeException(message)
