package io.lbert.server

import cats.data.{Kleisli, OptionT}
import io.circe.{Decoder, Encoder, Json}
import io.lbert.config.ServerConfig
import io.lbert.log.Logger
import io.lbert.metrics.Metrics
import io.lbert.metrics.Metrics.Names
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeServerBuilder
import zio.{Task, ZIO, ZManaged}
import org.http4s.implicits._
import zio.interop.catz._
import zio.interop.catz.implicits._

object Http4sServer {

  def run(
    serverConfig: ServerConfig,
    routes      : HttpRoutes[Task],
    logger      : Logger
  ): ZManaged[zio.ZEnv, Throwable, Unit] =
    ZManaged.fromEffect(
      logger.info(s"Starting server on port [${serverConfig.port.port}] bound to address [${serverConfig.bind.bind}]").flatMap(_ =>
        ZIO.runtime[zio.ZEnv].flatMap(implicit runtime =>
          BlazeServerBuilder[Task](runtime.platform.executor.asEC)
            .bindHttp(
              serverConfig.port.port,
              serverConfig.bind.bind
            )
            .withHttpApp(routes.orNotFound)
            .serve
            .compile[Task, Task, cats.effect.ExitCode]
            .drain
        )
      )
    )

  def compose(initial: HttpRoutes[Task])(routes: HttpRoutes[Task] => HttpRoutes[Task]*): HttpRoutes[Task] =
    routes.foldLeft(initial)((a, b) => b(a))

  def accessLogsMiddleware(
    service: HttpRoutes[Task],
    logger : Logger
  ): HttpRoutes[Task] =
    Kleisli[OptionT[Task, *], Request[Task], Response[Task]] { (req: Request[Task]) =>
      service(req).flatMap { resp =>
        OptionT.liftF[Task, Response[Task]](
          logger.info(accessLogJson(req, resp.status.code)).as(resp)
        )
      }.orElseF(
        logger.info(accessLogJson(req, 404)).as(None)
      )
    }

  def accessLogJson[F[_]](
    req       : Request[F],
    statusCode: Int
  ): Json = Json.obj(
    "method" -> Json.fromString(req.method.name),
    "uri"    -> Json.fromString(req.uri.renderString),
    "status" -> Json.fromInt(statusCode)
  )

  def metricsMiddleware(
    service: HttpRoutes[Task],
    metrics: Metrics
  ): HttpRoutes[Task] =
    Kleisli[OptionT[Task, *], Request[Task], Response[Task]] { (req: Request[Task]) =>
      OptionT[Task, Response[Task]](metrics.time(Names(req.method.name, req.uri.path))(service(req).value))
    }

  object Implicits {

    val http4sDsl: Http4sDsl[Task] = Http4sDsl[Task]

    implicit def jsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[Task, A] =
      org.http4s.circe.jsonEncoderOf[Task, A]

    implicit def jsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[Task, A] =
      org.http4s.circe.jsonOf[Task, A]

    implicit val stringEncoder: EntityEncoder[Task, String] = EntityEncoder.stringEncoder
    implicit val stringDecoder: EntityDecoder[Task, String] = EntityDecoder.text
  }
}
