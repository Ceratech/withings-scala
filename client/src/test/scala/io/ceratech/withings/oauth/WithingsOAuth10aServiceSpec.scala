package io.ceratech.withings.oauth

import java.io.ByteArrayInputStream
import java.util

import com.github.scribejava.core.httpclient.HttpClient
import com.github.scribejava.core.model._
import io.ceratech.withings.helper.ConfigHelper.constructConfig
import io.ceratech.withings.{BaseTest, WithingsException}
import org.mockito.ArgumentMatchers.{any, eq ⇒ asEq}
import org.mockito.Mockito._
import play.api.libs.json.{Json, Reads}

/**
  * Test the service
  *
  * @author dries
  */
class WithingsOAuth10aServiceSpec extends BaseTest {

  "The WithingOAuth10aService" when {
    "getAuthorizationUrl" should {
      "add OAuth parameters" in {
        val client = mock[HttpClient]

        val service = new WithingsOAuth10aService(constructConfig(client))
        val requestToken = new OAuth1RequestToken("token", "token_secret")

        val url = service.getAuthorizationUrl(requestToken)

        url must include("oauth_consumer_key")
        url must include("oauth_nonce")
        url must include("oauth_signature")
        url must include("oauth_signature_method")
        url must include("oauth_timestamp")
        url must include("oauth_version")
        url must include("oauth_token")
      }

    }

    "executeAsCompletable" should {
      "complete successfully on a response with a 200 code" in {
        val client = mock[HttpClient]

        val service = new WithingsOAuth10aService(constructConfig(client))
        val request = new OAuthRequest(Verb.GET, "http://url.tld")
        val response = new Response(200, "", new util.HashMap[String, String](), "")

        when(client.execute(any[String], any[java.util.Map[String, String]], asEq(Verb.GET), asEq(request.getCompleteUrl), any[Array[Byte]])) thenReturn response

        service.executeAsCompletable(request).map { _ ⇒
          succeed
        }
      }

      "fail on a response with a non 200 code" in {
        val client = mock[HttpClient]

        val service = new WithingsOAuth10aService(constructConfig(client))
        val request = new OAuthRequest(Verb.GET, "http://url.tld")
        val response = new Response(400, "", new util.HashMap[String, String](), "")

        when(client.execute(any[String], any[java.util.Map[String, String]], asEq(Verb.GET), asEq(request.getCompleteUrl), any[Array[Byte]])) thenReturn response

        recoverToSucceededIf[WithingsException] {
          service.executeAsCompletable(request)
        }
      }
    }

    "executeAsJson" should {
      "complete successfully when the response contains a valid JSON body" in {
        val client = mock[HttpClient]

        val service = new WithingsOAuth10aService(constructConfig(client))
        val request = new OAuthRequest(Verb.GET, "http://url.tld")
        val response = new Response(200, "", new util.HashMap[String, String](), new ByteArrayInputStream("{\"id\": 20}".getBytes))

        when(client.execute(any[String], any[java.util.Map[String, String]], asEq(Verb.GET), asEq(request.getCompleteUrl), any[Array[Byte]])) thenReturn response

        service.executeAsJson(request).map { _ ⇒
          succeed
        }
      }

      "fail when the response contains an unexpected JSON body" in {
        val client = mock[HttpClient]

        val service = new WithingsOAuth10aService(constructConfig(client))
        val request = new OAuthRequest(Verb.GET, "http://url.tld")
        val response = new Response(200, "", new util.HashMap[String, String](), new ByteArrayInputStream("{\"count\": 20}".getBytes))

        when(client.execute(any[String], any[java.util.Map[String, String]], asEq(Verb.GET), asEq(request.getCompleteUrl), any[Array[Byte]])) thenReturn response

        recoverToSucceededIf[WithingsException] {
          service.executeAsJson(request)
        }
      }
    }

    "the companion object" should {
      "create a service with defaults" in {
        val service = WithingsOAuth10aService("key", "secret", "callback")
        service.getConfig.getHttpClient mustBe null
      }
    }
  }

  case class TestJson(id: Long)

  private implicit val testReads: Reads[TestJson] = Json.reads[TestJson]
}
