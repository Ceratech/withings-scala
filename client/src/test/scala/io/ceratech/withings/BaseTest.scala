package io.ceratech.withings

import org.scalatest.{AsyncWordSpec, MustMatchers}
import org.scalatest.mockito.MockitoSugar

/**
  * Base unit-test class
  *
  * @author dries
  */
abstract class BaseTest
  extends AsyncWordSpec
    with MustMatchers
    with MockitoSugar