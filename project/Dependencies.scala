import sbt._

object Dependencies {

  object Test {
    lazy val scalaTest  = "org.scalatest"  %% "scalatest"  % "3.0.5"
    lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.14.0"
  }

  object Cats {
    lazy val core   = "org.typelevel" %% "cats-core"   % "2.0.0"
    lazy val effect = "org.typelevel" %% "cats-effect" % "2.1.4"
  }

  object ZIO {
    lazy val core        = "dev.zio" %% "zio"                 % "1.0.0-RC21-2"
    lazy val config      = "dev.zio" %% "zio-config"          % "1.0.0-RC25"
    lazy val typesafe    = "dev.zio" %% "zio-config-typesafe" % "1.0.0-RC25"
    lazy val catsInterop = "dev.zio" %% "zio-interop-cats"    % "2.1.4.0-RC17"
    lazy val metrics     = "dev.zio" %% "zio-metrics-statsd"  % "0.2.8"
  }

  object Http4s {
    lazy val version = "0.21.6"

    lazy val server = "org.http4s"      %% "http4s-blaze-server" % version
    lazy val client = "org.http4s"      %% "http4s-blaze-client" % version
    lazy val circe  = "org.http4s"      %% "http4s-circe"        % version
    lazy val dsl    = "org.http4s"      %% "http4s-dsl"          % version
  }

  object Circe {
    lazy val version = "0.12.3"

    lazy val core    = "io.circe" %% "circe-core"    % version
    lazy val generic = "io.circe" %% "circe-generic" % version
    lazy val parser  = "io.circe" %% "circe-parser"  % version
  }

  object FS2 {
    lazy val fs2   = "co.fs2" %% "fs2-core" % "1.0.1"
    lazy val fs2IO = "co.fs2" %% "fs2-io"   % "1.0.1"
  }

  object Logging {
    lazy val logback = "ch.qos.logback" % "logback-classic" % "1.1.2"
  }

  object Plugins {
    lazy val kindProjector    = "org.typelevel" %% "kind-projector"     % "0.11.0" cross CrossVersion.full
    lazy val betterMonadicFor = "com.olegpy"    %% "better-monadic-for" % "0.3.1"
  }

}
