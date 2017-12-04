package io.ceratech.withings

import java.time.{ZoneId, ZonedDateTime}
import java.util

import com.github.scribejava.core.model.{OAuth1AccessToken, OAuth1RequestToken, OAuthRequest, Response}
import io.ceratech.withings.model._
import io.ceratech.withings.oauth.WithingsOAuth10aService
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
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
        val callback = "callback"

        when(service.getRequestToken(callback)) thenReturn Future.successful(requestToken)
        when(service.getAuthorizationUrl(requestToken)) thenReturn authUrl

        client.fetchAuthorizationUrl(callback).map {
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

        val timezone = "Europe/Paris"
        val date = ZonedDateTime.now(ZoneId.of(timezone)).withNano(0) // Nanos are ignored in parsing JSON so skip it :)
        val measurement = Measurement(20, 10, 5)
        val grp = MeasurementGrp(1L, 2, date.toEpochSecond, 2, measurement :: Nil)
        val group = MeasurementGroup(1L, 2, date, 2, measurement :: Nil)
        val response = MeasurementResponse(timezone, grp :: Nil)

        when(service.executeAsJson(any[OAuthRequest])(any[Reads[WithingsResponse[MeasurementResponse]]])) thenReturn Future.successful(WithingsResponse(0, Some(response), None))

        client.getMeasurements(userId, start, stop).map { list ⇒
          verify(service).signRequest(any[OAuth1AccessToken], any[OAuthRequest])

          list must have length 1
          list must contain(group)
        }
      }

      "propagate any API error" in {
        val service = mock[WithingsOAuth10aService]
        val client = new WithingsClient(service)

        implicit val withingsAccessToken: WithingsAccessToken = WithingsAccessToken("token", "secret")

        val response = MeasurementResponse("Test", Nil)
        when(service.executeAsJson(any[OAuthRequest])(any[Reads[WithingsResponse[MeasurementResponse]]])) thenReturn Future.successful(WithingsResponse(503, Some(response), Some("Invalid params")))

        recoverToSucceededIf[WithingsException] {
          client.getMeasurements(1, ZonedDateTime.now(), ZonedDateTime.now())
        }
      }
    }

    "the companion object" should {
      "create an initalized client" in {
        val client = WithingsClient("key", "secret")
        client must not be null
      }
    }
  }
}
