package io.ceratech.withings.rest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}

/**
  * Test the route resrouce
  *
  * @author dries
  */
class WithingsResourceSpec extends WordSpec
  with MustMatchers
  with MockitoSugar
  with ScalatestRouteTest
  with BeforeAndAfterEach {



  "The WithingsResource" when {
    "GET authorizationUrl" should {
      "return URL and temporary tokens" in {

      }
    }
  }

}
