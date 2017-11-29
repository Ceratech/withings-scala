package io.ceratech.withings.rest

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.scribejava.core.model.OAuth1RequestToken
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import io.ceratech.withings.model.MeasurementGroup
import io.ceratech.withings.rest.model._
import io.ceratech.withings.{WithingsAccessToken, WithingsClient}
import io.swagger.annotations._

import scala.concurrent.ExecutionContext

/**
  * REST resource for the Withings client
  *
  * @author dries
  */
@Api(value = "/withings", produces = "application/json")
@Path("/withings")
class WithingsResource(client: WithingsClient)(implicit executionContext: ExecutionContext)
  extends RestJsonMapping
    with PlayJsonSupport {

  @Path("/withings")
  lazy val routes: Route =
    path("withings") {
      auth ~ calls
    }

  @Path("auth")
  private lazy val auth: Route =
    path("auth") {
      authorizationUrl ~ requestAccessToken
    }

  @Path("authorizationUrl")
  @ApiOperation(value = "Get Withings authorization URL", nickname = "authorizationUrl", httpMethod = "GET", response = classOf[AutorizationRequestResult])
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  private def authorizationUrl: Route = {
    path("authorizationUrl") {
      get {
        val future = client.fetchAuthorizationUrl.map {
          case (url, requestToken) ⇒ AutorizationRequestResult(url, requestToken.getToken, requestToken.getTokenSecret)
        }

        onSuccess(future) {
          complete(_)
        }
      }
    }
  }

  @ApiOperation(value = "Request a Withings access token", nickname = "requestAccessToken", httpMethod = "POST", response = classOf[WithingsAccessToken])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "parameters required for the access token call", required = true,
      dataTypeClass = classOf[AccessTokenRequest], paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  private def requestAccessToken: Route = {
    path("requestAccessToken") {
      post {
        entity(as[AccessTokenRequest]) { request ⇒
          val token = new OAuth1RequestToken(request.tempToken, request.tempTokenSecret)
          onSuccess(client.requestAccessToken(token, request.verifier)) {
            complete(_)
          }
        }
      }
    }
  }

  private lazy val calls: Route =
    path("calls") {
      registerNotification ~ measurements
    }

  @ApiOperation(value = "Register a notification when a certain event hapens on the Withings API", nickname = "registerNotification", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "parameters required for the notification call", required = true,
      dataTypeClass = classOf[RegisterNotificationParameters], paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  private def registerNotification: Route = {
    path("registerNotification") {
      post {
        entity(as[WithingsClientRequest[RegisterNotificationParameters]]) { body ⇒
          implicit val accessToken: WithingsAccessToken = WithingsAccessToken(body.token, body.tokenSecret)

          val parameters = body.parameters
          onSuccess(client.registerNotification(parameters.userId, parameters.callback, parameters.comment, parameters.application)) {
            complete(OK)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Get users measurements in a certain timeframe", nickname = "measurements", httpMethod = "POST", response = classOf[Seq[MeasurementGroup]])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "parameters required for the measurements call", required = true,
      dataTypeClass = classOf[MeasurementsParameters], paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  private def measurements: Route = {
    path("measurements") {
      post {
        entity(as[WithingsClientRequest[MeasurementsParameters]]) { body ⇒
          implicit val accessToken: WithingsAccessToken = WithingsAccessToken(body.token, body.tokenSecret)

          val parameters = body.parameters
          onSuccess(client.getMeasurements(parameters.userId, parameters.startDate, parameters.endDate)) { measurements ⇒
            complete(measurements)
          }
        }
      }
    }
  }
}
