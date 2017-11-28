import Dependencies._

enablePlugins(GitVersioning)

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
      // OAuth 1.0 client
      "com.github.scribejava" % "scribejava-core" % "5.0.0",

      // JSON support
      "com.typesafe.play" %% "play-json" % "2.6.7"
    ) ++ testStack
  )

lazy val amqp = (project in file("amqp"))
  .dependsOn(client)
  .settings(commonSettings,
    name := "withings-amqp-rpc",

    libraryDependencies ++= Seq(
      "com.rabbitmq" % "amqp-client" % "5.0.0",

      "com.typesafe" % "config" % "1.3.2",
      "com.github.pureconfig" %% "pureconfig" % "0.8.0"
    ) ++ testStack
  )