import sbt._

object Dependencies {
  private lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
  private lazy val mockito = "org.mockito" % "mockito-core" % "2.12.0" % Test

  val testStack: Seq[ModuleID] = scalaTest :: mockito :: Nil
}
