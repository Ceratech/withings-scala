package io.ceratech.withings.rest

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.Unauthorized
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import com.github.scribejava.core.exceptions.OAuthException
import com.typesafe.scalalogging.LazyLogging

/**
  * Error handler for the REST API
  *
  * @author dries
  */
trait ErrorHandler extends LazyLogging {

  val oAuthExceptionHandler = ExceptionHandler {
    case ex: OAuthException =>
      extractUri { uri =>
        logger.error(s"Error executing call to $uri", ex.getMessage)
        complete(HttpResponse(Unauthorized, entity = ex.getMessage))
      }
  }

}
