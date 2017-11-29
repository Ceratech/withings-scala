package io.ceratech.withings.rest

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.scribejava.core.model.OAuth1RequestToken
import io.ceratech.withings.model.{Measurement, MeasurementGroup}
import io.ceratech.withings.rest.model.RestJsonMapping
import io.ceratech.withings.{WithingsAccessToken, WithingsClient}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}

import scala.concurrent.Future

/**
  * Test the route resrouce
  *
  * @author dries
  */
class WithingsResourceSpec extends WordSpec
  with MustMatchers
  with MockitoSugar
  with ScalatestRouteTest
  with RestJsonMapping {

  "The WithingsResource" when {
    "GET authorizationUrl" should {
      "return URL and temporary tokens" in {
        val client = mock[WithingsClient]
        val resource = new WithingsResource(client)

        val (url, token) = ("url", new OAuth1RequestToken("temp-token", "temp-secret"))
        when(client.fetchAuthorizationUrl) thenReturn Future.successful((url, token))

        Get("/auth/authorizationUrl") ~> resource.routes ~> check {
          status mustBe OK
          contentType mustBe `application/json`

          val content = responseAs[String]
          content must include("url")
          content must include("tempToken")
          content must include("tempTokenSecret")
        }
      }
    }

    "POST requestAccessToken" should {
      "return a valid API access token with a valid temp token and verification code" in {
        val client = mock[WithingsClient]
        val resource = new WithingsResource(client)

        val tempToken = new OAuth1RequestToken("temp-token", "temp-secret")
        val verifier = "verifier_code"
        val (token, secret) = ("token", "secret")

        when(client.requestAccessToken(tempToken, verifier)) thenReturn Future.successful(WithingsAccessToken(token, secret))

        val body =
          s"""{
             |  "tempToken": "${tempToken.getToken}",
             |  "tempTokenSecret": "${tempToken.getTokenSecret}",
             |  "verifier": "$verifier"
             |}""".stripMargin

        Post("/auth/requestAccessToken", HttpEntity(`application/json`, body)) ~> resource.routes ~> check {
          status mustBe OK
          contentType mustBe `application/json`

          val content = responseAs[String]
          content must include("token")
          content must include("tokenSecret")
        }
      }
    }

    "POST registerNotification" should {
      "return a 200 response when the notification is successfully posted" in {
        val client = mock[WithingsClient]
        val resource = new WithingsResource(client)

        val userId = 1L
        val callback = "https://notification.callback/url"
        val comment = "Notification comment"
        val application = 4

        val accessToken = WithingsAccessToken("token", "secret")

        when(client.registerNotification(userId, callback, comment, application)(accessToken)) thenReturn Future.successful(())

        val body =
          s"""{
             |  "token": "${accessToken.token}",
             |  "tokenSecret": "${accessToken.tokenSecret}",
             |  "parameters": {
             |    "userId": $userId,
             |    "callback": "$callback",
             |    "comment": "$comment",
             |    "application": $application
             |  }
             |}""".stripMargin

        Post("/calls/registerNotification", HttpEntity(`application/json`, body)) ~> resource.routes ~> check {
          status mustBe OK
        }
      }
    }

    "POST measurements" should {
      "get a list of measurements for the provided user and date range" in {
        val client = mock[WithingsClient]
        val resource = new WithingsResource(client)

        val dateFormatter = DateTimeFormatter.ISO_DATE_TIME

        val userId = 1L
        val startDate = ZonedDateTime.now().minusDays(5)
        val endDate = ZonedDateTime.now().plusDays(5)

        val accessToken = WithingsAccessToken("token", "secret")

        val response = MeasurementGroup(1L, 4, ZonedDateTime.now().toEpochSecond, 4, Measurement(20, 30, 40) :: Nil) :: Nil
        when(client.getMeasurements(userId, startDate, endDate)(accessToken)) thenReturn Future.successful(response)

        val body =
          s"""{
             |  "token": "${accessToken.token}",
             |  "tokenSecret": "${accessToken.tokenSecret}",
             |  "parameters": {
             |    "userId": $userId,
             |    "startDate": "${startDate.format(dateFormatter)}",
             |    "endDate": "${endDate.format(dateFormatter)}"
             |  }
             |}""".stripMargin

        Post("/calls/measurements", HttpEntity(`application/json`, body)) ~> resource.routes ~> check {
          status mustBe OK
        }
      }
    }
  }

}
