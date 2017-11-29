package io.ceratech.withings.rest.swagger

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import io.ceratech.withings.BuildInfo
import io.ceratech.withings.rest.WithingsResource

/**
  * Swagger docs service
  *
  * @author dries
  */
object SwaggerDocService extends SwaggerHttpService {
  override val apiClasses = Set(classOf[WithingsResource])
  override val info = Info(version = BuildInfo.version, description = "Withings API wrapper interface", title = BuildInfo.name)
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")
}
