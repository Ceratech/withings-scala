package io.ceratech.withings.rest.swagger

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{MustMatchers, WordSpec}

/**
  * Test if Swagger docs include all routes
  *
  * @author dries
  */
class SwaggerDocServiceSpec
  extends WordSpec
    with MustMatchers
    with ScalatestRouteTest {

  "The SwaggerDocService" should {
    "provide a JSON route" in {
      val service = SwaggerDocService

      Get("/api-docs/swagger.json") ~> service.routes ~> check {
        status mustBe OK
        contentType mustBe `application/json`

        val content = responseAs[String]
        content must include("auth/authorizationUrl")
        content must include("auth/requestAccessToken")
        content must include("calls/registerNotification")
        content must include("calls/measurements")
      }
    }
  }
}
