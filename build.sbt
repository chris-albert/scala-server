
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "io.lbert",
      scalaVersion := "2.12.10",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "scala-server",
    libraryDependencies ++= Seq(
      Dependencies.ZIO.core,
      Dependencies.ZIO.config,
      Dependencies.ZIO.typesafe,
      Dependencies.ZIO.catsInterop,
      Dependencies.ZIO.metrics,
      Dependencies.Http4s.server,
      Dependencies.Http4s.client,
      Dependencies.Http4s.circe,
      Dependencies.Http4s.dsl,
      Dependencies.Circe.core,
      Dependencies.Circe.parser,
      Dependencies.Cats.core,
      Dependencies.Cats.effect,
      Dependencies.Logging.logback,
      Dependencies.Test.scalaTest % Test
    ),
    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-Xfatal-warnings",
      "-deprecation",
      "-unchecked",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:existentials",
      "-language:postfixOps"
    ),
    resolvers += Resolver.sonatypeRepo("releases"),
    addCompilerPlugin(Dependencies.Plugins.kindProjector),
    addCompilerPlugin(Dependencies.Plugins.betterMonadicFor)
  )
