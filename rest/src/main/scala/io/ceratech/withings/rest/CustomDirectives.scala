package io.ceratech.withings.rest

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.{Unauthorized, BadRequest}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, ExceptionHandler}
import com.github.scribejava.core.exceptions.OAuthException
import com.typesafe.scalalogging.LazyLogging
import io.ceratech.withings.{WithingsAccessToken, WithingsException}

/**
  * Error handler for the REST API
  *
  * @author dries
  */
trait CustomDirectives extends LazyLogging {

  /**
    * Handles [[OAuthException]]s and produces error messages
    */
  val oAuthExceptionHandler = ExceptionHandler {
    case ex: OAuthException =>
      extractUri { uri =>
        logger.error(s"Error executing call to $uri", ex.getMessage)
        complete(HttpResponse(Unauthorized, entity = ex.getMessage))
      }
    case WithingsException(msg) ⇒
      extractUri { uri =>
        logger.error(s"Error executing call to $uri", msg)
        complete(HttpResponse(BadRequest, entity = msg))
      }
  }

  /**
    * Extract the API tokens and construct a [[WithingsAccessToken]] instance
    */
  val extractTokens: Directive[Tuple1[WithingsAccessToken]] = (headerValueByName("X-Api-Token") & headerValueByName("X-Api-Secret")).tmap {
    case (token, secret) ⇒ WithingsAccessToken(token, secret)
  }
}
