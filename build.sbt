import Dependencies._
import sbt.util

enablePlugins(GitVersioning)

val akkaVersion = "2.5.7"
val akkaHttpVersion = "10.0.10"

lazy val commonSettings = Seq(
  organization := "io.ceratech",
  scalaVersion := "2.12.4",

  git.useGitDescribe := true,
  git.baseVersion := "0.1",

  bintrayOrganization := Some("ceratech"),
  licenses += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))
)

lazy val client = (project in file("client"))
  .settings(commonSettings,
    name := "withings-client",

    libraryDependencies ++= Seq(
      scalaLogging,

      // For tagging model classes
      "io.swagger" % "swagger-annotations" % "1.5.17" % Provided,

      // OAuth 1.0 client
      "com.github.scribejava" % "scribejava-core" % "5.0.0",

      // JSON support
      "com.typesafe.play" %% "play-json" % "2.6.7"
    ) ++ testStack
  )

lazy val rest = (project in file("rest"))
  .dependsOn(client)
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings,
    name := "withings-rest",

    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "io.ceratech.withings",

    libraryDependencies ++= Seq(
      scalaLogging,

      // Akka HTTP
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,

      // Play JSON support
      "de.heikoseeberger" %% "akka-http-play-json" % "1.18.0",

      // Swagger
      "io.swagger" % "swagger-jaxrs" % "1.5.17",
      "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.11.1",
      "org.webjars" % "swagger-ui" % "3.5.0",

      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "ch.qos.logback" % "logback-classic" % "1.2.3" % Runtime
    ) ++ testStack
  )