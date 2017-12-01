package io.ceratech.withings.oauth

import com.github.scribejava.core.model._
import com.github.scribejava.core.oauth.OAuth10aService
import io.ceratech.withings.{WithingsApi, WithingsException}
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.collection.JavaConverters._
/**
  * Custom service to allow creation of a more advanced authorization URL
  *
  * @author dries
  */
class WithingsOAuth10aService(config: OAuthConfig)(implicit executionContext: ExecutionContext) extends OAuth10aService(WithingsApi, config) {

  override def getAuthorizationUrl(requestToken: OAuth1RequestToken): String = {
    val request = new OAuthRequest(Verb.GET, getApi.getAuthorizationUrl(requestToken))

    // Withings API requires all extra OAuth parameters to be present as well
    addOAuthParams(request, "")
    request.getOauthParameters.asScala.foreach {
      case (key, value) ⇒ request.addQuerystringParameter(key, value)
    }

    request.getCompleteUrl
  }

  /**
    * Fetch the request token with a custom callback URL
    *
    * @param callbackUrl the callback URL to use
    * @return a [[OAuth1AccessToken]]
    */
  def getRequestToken(callbackUrl: String): Future[OAuth1RequestToken] = {
     val request = new OAuthRequest(getApi.getRequestTokenVerb, getApi.getRequestTokenEndpoint)
    request.addOAuthParameter(OAuthConstants.CALLBACK, callbackUrl)
    addOAuthParams(request, "")
    appendSignature(request)

    executeCall(request).map(response ⇒ getApi.getRequestTokenExtractor.extract(response))
  }

  def executeAsCompletable(request: OAuthRequest): Future[Response] = {
    executeCall(request).map(checkResponse)
  }

  def executeAsJson[T](request: OAuthRequest)(implicit reads: Reads[T]): Future[T] = {
    executeCall(request).map(checkResponse).map { response ⇒
      val json = Json.parse(response.getStream)
      json.validate[T] match {
        case JsSuccess(res, _) ⇒ res
        case JsError(errors) ⇒
          // Readable error for end-user
          val details = errors.map {
            case (path, validationErrors) ⇒ s"${path.toString()}:\n\t${validationErrors.map(_.messages.mkString(", ")).mkString("\n\t")}"
          }.mkString("\n")
          throw WithingsException(s"Error reading JSON response\n$details")
      }
    }
  }

  private def executeCall(request: OAuthRequest): Future[Response] = {
    Future {
      blocking(execute(request))
    }
  }

  private def checkResponse(response: Response): Response = {
    if (response.getCode != 200) {
      throw WithingsException(s"Error executing request; code: ${response.getCode}, message: ${response.getMessage}")
    }

    response
  }
}

object WithingsOAuth10aService {

  /**
    * Creates a [[WithingsOAuth10aService]]
    *
    * @param apiKey    the API key
    * @param apiSecret the API secret key
    * @return an [[WithingsOAuth10aService]] instance
    */
  def apply(apiKey: String, apiSecret: String)(implicit executionContext: ExecutionContext): WithingsOAuth10aService = {
    val config = new OAuthConfig(apiKey, apiSecret, null, null, null, null, null, null, null, null)
    new WithingsOAuth10aService(config)
  }
}
