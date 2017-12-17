package io.ceratech.withings.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import io.ceratech.withings.WithingsClient
import io.ceratech.withings.rest.swagger.SwaggerDocService

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Properties

/**
  * Main class that starts the server
  *
  * @author dries
  */
trait Server extends LazyLogging {
  // Read needed properties to boot the server
  val port: Int = Properties.envOrElse("PORT", "8080").toInt
  val apiKey: String = Properties.envOrElse("API_KEY", "<unkown>")
  val apiSecret: String = Properties.envOrElse("API_SECRET", "<unkown>")

  // Setup API client and REST resources
  lazy val withingsClient: WithingsClient = WithingsClient(apiKey, apiSecret)
  lazy val withingsResource: WithingsResource = new WithingsResource(withingsClient)

  // Initialize Akka
  implicit val system: ActorSystem = ActorSystem("withings-rest")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Setup routes and start server
  lazy val routes: Route = withingsResource.routes ~ SwaggerDocService.routes

  /**
    * Runs the server
    */
  def startServer(): Unit = {
    val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "0.0.0.0", port)

    logger.info(s"Server started on port $port")

    // Shutdown hook to stop server on JVM shutdown
    sys.addShutdownHook {
      bindingFuture.flatMap(_.unbind())
        .onComplete(_ ⇒ system.terminate())
    }
    ()
  }
}