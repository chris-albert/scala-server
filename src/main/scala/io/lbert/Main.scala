package io.lbert

import io.lbert.client.HTTPClient
import io.lbert.config.AppConfig
import io.lbert.file.FileZIO
import io.lbert.log.Logger
import io.lbert.metrics.Metrics
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
    config     <- AppConfig.live
    clock      <- ZManaged.environment[Clock].map(_.get)
    logger     <- Logger.ofName("service")
    access     <- Logger.ofName("access")
    file       <- FileZIO.live
    httpClient <- HTTPClient.live
    metrics    <- Metrics.statsD(config.metrics, logger, clock)
    httpRoutes  = Http4sServer.compose(routes)(
      Http4sServer.accessLogsMiddleware(_, access),
      Http4sServer.metricsMiddleware(_, metrics)
    )
    _          <- Http4sServer.run(config.server, httpRoutes, logger)
  } yield ()

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    app.use(_ => IO.unit).exitCode

}
