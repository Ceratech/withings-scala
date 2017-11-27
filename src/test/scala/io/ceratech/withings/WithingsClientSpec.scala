package io.ceratech.withings

import java.time.ZonedDateTime
import java.util

import com.github.scribejava.core.httpclient.HttpClient
import com.github.scribejava.core.model.{OAuth1AccessToken, OAuth1RequestToken, OAuthRequest, Response}
import io.ceratech.withings.helper.ConfigHelper.constructConfig
import io.ceratech.withings.model.{Measurement, MeasurementGroup, MeasurementResponse, WithingsResponse}
import io.ceratech.withings.oauth.WithingsOAuth10aService
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Reads

import scala.concurrent.Future

/**
  * Unit-tests for the Withings API client
  *
  * @author dries
  */
class WithingsClientSpec extends BaseTest {

  "The WithingsClient" when {
    "fetchAuthorizationUrl" should {
      "fetch a token and construct the authorization URL" in {
        val service = mock[WithingsOAuth10aService]
        val client = new WithingsClient(service)

        val requestToken = new OAuth1RequestToken("request_token", "request_token_secret")
        val authUrl = "http://auth.url/"

        when(service.getRequestToken) thenReturn requestToken
        when(service.getAuthorizationUrl(requestToken)) thenReturn authUrl

        client.fetchAuthorizationUrl.map {
          case (url, token) ⇒
            token mustBe requestToken
            url mustBe authUrl
        }
      }
    }

    "requestAccessToken" should {
      "call the API with the request token and verifier code" in {
        val service = mock[WithingsOAuth10aService]
        val client = new WithingsClient(service)

        val (token, tokenSecret) = ("token", "secret")

        val requestToken = new OAuth1RequestToken("request_token", "request_token_secret")
        val verifierCode = "code"

        when(service.getAccessToken(requestToken, verifierCode)) thenReturn new OAuth1AccessToken(token, tokenSecret)

        client.requestAccessToken(requestToken, verifierCode).map { accessToken ⇒
          accessToken.token mustBe token
          accessToken.tokenSecret mustBe tokenSecret
        }
      }
    }

    "registerNotification" should {
      "call the API to register a notification" in {
        val service = mock[WithingsOAuth10aService]
        val client = new WithingsClient(service)

        implicit val withingsAccessToken: WithingsAccessToken = WithingsAccessToken("token", "secret")

        when(service.executeAsCompletable(any[OAuthRequest])) thenReturn Future.successful(new Response(200, "test", new util.HashMap[String, String](), "body"))

        client.registerNotification(1L, "callback", "test comment", 4).map { _ ⇒
          verify(service).signRequest(any[OAuth1AccessToken], any[OAuthRequest])
          succeed
        }
      }
    }

    "getMeasurements" should {
      "call the API to fetch the measurements" in {
        val service = mock[WithingsOAuth10aService]
        val client = new WithingsClient(service)

        implicit val withingsAccessToken: WithingsAccessToken = WithingsAccessToken("token", "secret")

        val (start, stop) = (ZonedDateTime.now(), ZonedDateTime.now())
        val userId = 1L

        val measurement = Measurement(20, 10, 5)
        val group = MeasurementGroup(1L, 2, ZonedDateTime.now().toEpochSecond, 2, measurement :: Nil)
        val response = MeasurementResponse(group :: Nil)

        when(service.executeAsJson(any[OAuthRequest])(any[Reads[WithingsResponse[MeasurementResponse]]])) thenReturn Future.successful(WithingsResponse(200, Some(response)))

        client.getMeasurements(userId, start, stop).map { list ⇒
          verify(service).signRequest(any[OAuth1AccessToken], any[OAuthRequest])

          list must have length 1
          list must contain(group)
        }
      }
    }
  }
}
