name := "withings-scala"

enablePlugins(GitVersioning)

scalaVersion := "2.12.4"

git.baseVersion := "0.1"
git.useGitDescribe := true

autoScalaLibrary := true

bintrayOrganization := Some("ceratech")
licenses += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))

libraryDependencies ++= Seq(
  // OAuth 1.0 client
  "com.github.scribejava" % "scribejava-core" % "5.0.0",

  // JSON support
  "com.typesafe.play" %% "play-json" % "2.6.7",

  // Test stack
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "org.mockito" % "mockito-core" % "2.12.0" % Test
)