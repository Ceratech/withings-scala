import sbt._

object Dependencies {
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

  private lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
  private lazy val mockito = "org.mockito" % "mockito-core" % "2.12.0" % Test
  private lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3" % Test

  val testStack: Seq[ModuleID] = scalaTest :: mockito :: logback :: Nil
}
