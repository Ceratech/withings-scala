package io.ceratech.withings

import java.time.ZonedDateTime

import com.github.scribejava.core.model.{OAuth1RequestToken, OAuthRequest, Verb}
import io.ceratech.withings.Implicits._
import io.ceratech.withings.model.{JsonMapping, MeasurementGroup, MeasurementResponse, WithingsResponse}
import io.ceratech.withings.oauth.WithingsOAuth10aService
import play.api.libs.json.{JsError, JsSuccess, Json, JsonNaming}

import scala.concurrent.{ExecutionContext, Future, blocking}

/**
  * Withings API client
  *
  * @author dries
  */
class WithingsClient(apiKey: String,
                     apiSecret: String,
                     callback: String)
                    (implicit executionContext: ExecutionContext)
  extends JsonMapping {

  /** Auth service */
  private lazy val service: WithingsOAuth10aService = WithingsOAuth10aService(apiKey, apiSecret, callback)

  private def fetchRequestToken: Future[OAuth1RequestToken] = {
    Future {
      blocking {
        service.getRequestTokenAsync.get()
      }
    }
  }

  /**
    * @return the URL to redirect the user to authorize the application and the original request token
    */
  def fetchAuthorizationUrl: Future[(String, OAuth1RequestToken)] = {
    fetchRequestToken.map { requestToken ⇒
      (service.getAuthorizationUrl(requestToken), requestToken)
    }
  }

  /**
    * Last step; request an access token with initial request token and user verification code
    *
    * @param token    the intial request token
    * @param verifier the user verification code; obtained from callback on the authorization call
    * @return the [[WithingsAccessToken]] to use for subsequent requests to the API
    */
  def requestAccessToken(token: OAuth1RequestToken, verifier: String): Future[WithingsAccessToken] = {
    Future {
      blocking {
        service.getAccessTokenAsync(token, verifier).get()
      }
    }.map(t ⇒ WithingsAccessToken(t.getToken, t.getTokenSecret))
  }

  /**
    * Register a callback to trigger when a new measurement is recorded in the API
    *
    * @param userId      for this user
    * @param callback    the callback url to call
    * @param comment     the comment to show in the overview (i.e. what notification this is)
    * @param application the data to listen for
    * @param accessToken the Withings API tokens
    * @return completion of this action
    */
  def registerNotification(userId: Long, callback: String, comment: String, application: Int)(implicit accessToken: WithingsAccessToken): Future[Unit] = {
    val request = new OAuthRequest(Verb.POST, "https://api.health.nokia.com/notify?action=subscribe")
    request.addBodyParameters(Map(
      "userid" → userId,
      "callbackurl" → callback,
      "comment" → comment,
      "appli" → application
    ))
    service.signRequest(accessToken.oauthAccessToken, request)

    service.executeAsCompletable(request).map(_ ⇒ ())
  }

  /**
    * Fetches measurements for a given users within a given time period
    *
    * @param userId    for this user
    * @param startDate the start of the fetch period
    * @param endDate   the end of the fetch period
    * @return all measurements within the given time period
    */
  def getMeasurements(userId: Long, startDate: ZonedDateTime, endDate: ZonedDateTime)(implicit accessToken: WithingsAccessToken): Future[Seq[MeasurementGroup]] = {
    val request = new OAuthRequest(Verb.POST, "https://api.health.nokia.com/measure?action=getmeas")
    request.addBodyParameters(Map(
      "userid" → userId,
      "startdate" → startDate.toEpochSecond,
      "enddate" → endDate.toEpochSecond
    ))
    service.signRequest(accessToken.oauthAccessToken, request)

    service.executeAsJson[WithingsResponse[MeasurementResponse]](request).map {
      _.body.map(_.measuregrps).getOrElse(Nil)
    }
  }
}
