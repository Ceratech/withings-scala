package io.ceratech.withings.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import io.ceratech.withings.WithingsClient
import io.ceratech.withings.rest.swagger.SwaggerDocService

import scala.concurrent.ExecutionContextExecutor
import scala.util.Properties

/**
  * Main class that starts the server
  *
  * @author dries
  */
object Server extends App with LazyLogging {
  // Read needed properties to boot the server
  val port = Properties.envOrElse("PORT", "8080").toInt
  val apiKey = Properties.envOrElse("API_KEY", "<unkown>")
  val apiSecret = Properties.envOrElse("API_SECRET", "<unkown>")

  // Setup API client and REST resources
  lazy val withingsClient = WithingsClient(apiKey, apiSecret)
  lazy val withingsResource = new WithingsResource(withingsClient)

  // Initialize Akka
  implicit val system: ActorSystem = ActorSystem("withings-rest")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Setup routes and start server
  lazy val routes = withingsResource.routes ~ SwaggerDocService.routes
  val bindingFuture = Http().bindAndHandle(routes, "0.0.0.0", port)

  logger.info(s"Server started on port $port")

  // Shutdown hook to stop server on JVM shutdown
  sys.addShutdownHook {
    bindingFuture.flatMap(_.unbind())
      .onComplete(_ ⇒ system.terminate())
  }
}
