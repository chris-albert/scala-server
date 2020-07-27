package io.lbert

import io.lbert.client.HTTPClient
import io.lbert.config.AppConfig
import io.lbert.file.FileZIO
import io.lbert.metrics.{Metrics, MetricsClient}
import io.lbert.server.{Health, Http4sServer}
import org.http4s.HttpRoutes
import org.http4s.server.Router
import zio._
import zio.clock.Clock
import zio.interop.catz._

object Main extends App {

  val routes: HttpRoutes[Task] = Router[Task](
    "/" -> Health.routes
  )

  val app = for {
    config        <- AppConfig.live
    clock         <- ZManaged.environment[Clock].map(_.get)
    logger        <- Logger.ofName("service")
    access        <- Logger.ofName("access")
    metricsLogger <- Logger.ofName("metrics")
    file          <- FileZIO.live
    httpClient    <- HTTPClient.live
    metricsClient <- MetricsClient.live(config.metrics, logger, clock)
    metrics       <- Metrics.live(metricsClient, clock)
    metricsL      <- Metrics.logger(metricsLogger, clock)
    httpRoutes     = Http4sServer.metricsMiddleware(Http4sServer.accessLogsMiddleware(routes, access), metricsL)
    _             <- Http4sServer.run(config.server, httpRoutes, logger)
  } yield ()

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    app.use(_ => IO.unit).exitCode

}
