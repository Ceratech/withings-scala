package io.ceratech.withings

import java.time.{Instant, ZoneId, ZonedDateTime}

import com.github.scribejava.core.model.{OAuth1RequestToken, OAuthRequest, Verb}
import com.typesafe.scalalogging.Logger
import io.ceratech.withings.Implicits._
import io.ceratech.withings.model._
import io.ceratech.withings.oauth.WithingsOAuth10aService

import scala.concurrent.{ExecutionContext, Future, blocking}

/**
  * Withings API client
  *
  * @author dries
  */
class WithingsClient(service: WithingsOAuth10aService)
                    (implicit executionContext: ExecutionContext)
  extends JsonMapping {

  private val logger = Logger[WithingsClient]

  private def fetchRequestToken(callbackUrl: String): Future[OAuth1RequestToken] = {
    logger.debug("Fetching request token")
    service.getRequestToken(callbackUrl)
  }

  /**
    * Fetches an temp token and the authorization URL to suply to the user
    *
    * @param callbackUrl the supplied callback to where the API will redirect the user with the verifier code
    * @return the URL to redirect the user to authorize the application and the original request token
    */
  def fetchAuthorizationUrl(callbackUrl: String): Future[(String, OAuth1RequestToken)] = {
    fetchRequestToken(callbackUrl).map { requestToken ⇒
      logger.debug("Fetching authorization token")
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
      logger.debug(s"Requesting access token with parameters: temp-token: ${token.getToken}, temp-secret: ${token.getTokenSecret}, verifier: $verifier")
      blocking(service.getAccessToken(token, verifier))
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

    logger.debug(s"Calling register notification with parmeters: userId: $userId, callbackurl: $callback, comment: $comment, appli: $application")
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
    val request = new OAuthRequest(Verb.GET, "https://api.health.nokia.com/measure")
    request.addQueryParameters(Map(
      "action" → "getmeas",
      "userid" → userId,
      "startdate" → startDate.toEpochSecond,
      "enddate" → endDate.toEpochSecond
    ))
    service.signRequest(accessToken.oauthAccessToken, request)

    // Construct a MeasurementGroup
    def mapGrpToGroup(grp: MeasurementGrp, zone: ZoneId): MeasurementGroup = {
      MeasurementGroup(grp.grpid, grp.attrib, ZonedDateTime.ofInstant(Instant.ofEpochSecond(grp.date), zone), grp.category, grp.measures)
    }

    logger.debug(s"Calling measurements with parmeters: userId: $userId, startdate: ${startDate.toEpochSecond}, enddate: ${endDate.toEpochSecond}")
    service.executeAsJson[WithingsResponse[MeasurementResponse]](request).map { response ⇒
      checkResponseCode(response)

      response.body.map { res ⇒
        val zone = ZoneId.of(res.timezone)
        res.measuregrps.map(mapGrpToGroup(_, zone))
      }.getOrElse(Nil)
    }
  }

  /**
    * Feteches the registered notifications (callbacks) from the API
    *
    * @param userId      for this user
    * @param accessToken using these API access tokens
    * @return the found registered notifications
    */
  def getRegisteredNotifications(userId: Long)(implicit accessToken: WithingsAccessToken): Future[Seq[RegisteredNotification]] = {
    val request = new OAuthRequest(Verb.GET, "https://api.health.nokia.com/notify")
    request.addQueryParameters(Map(
      "action" → "list",
      "userid" → userId
    ))
    service.signRequest(accessToken.oauthAccessToken, request)

    service.executeAsJson[WithingsResponse[NotificationProfiles]](request).map { response ⇒
      checkResponseCode(response)

      def mapProfileToRegistered(profile: NotificationProfile): RegisteredNotification = {
        RegisteredNotification(ZonedDateTime.ofInstant(Instant.ofEpochSecond(profile.expires), ZoneId.systemDefault()), profile.comment)
      }

      response.body.map { list ⇒
        list.profiles.map(mapProfileToRegistered)
      }.getOrElse(Nil)
    }
  }

  private def checkResponseCode(response: WithingsResponse[_]) = {
    if (response.status != 0) {
      throw WithingsException(s"API error: code: ${response.status}, message: ${response.error.getOrElse("<no message>")}")
    }
  }
}

object WithingsClient {

  /**
    * Construct a default Withings client
    *
    * @param apiKey           the API key to use
    * @param apiSecret        the API secret to use
    * @param executionContext the context to run async tasks on
    * @return a [[WithingsClient]]
    */
  def apply(apiKey: String, apiSecret: String)(implicit executionContext: ExecutionContext): WithingsClient = {
    new WithingsClient(WithingsOAuth10aService(apiKey, apiSecret))
  }
}