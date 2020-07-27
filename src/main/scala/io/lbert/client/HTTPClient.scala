package io.lbert.client

import io.circe.Json
import io.lbert.Error.HTTPClientError
import io.lbert.server.Http4sServer
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import zio.{IO, Task, ZIO, ZManaged}
import zio.interop.catz._
import org.http4s.{EntityDecoder, Request}
import io.lbert.Error

trait HTTPClient {

  def withHTTP4SClient[A](f: Client[Task] => Task[A]): IO[Error, A]

  private def asA[A](request: Request[Task])(decoder: EntityDecoder[Task, A]): IO[Error, A] =
    withHTTP4SClient(_.run(request).use[Task, A](
      _.attemptAs[A](decoder)
        .value.flatMap(e => IO.fromEither(e))
    ))

  def asJson(request: Request[Task]): IO[Error, Json] =
    asA[Json](request)(Http4sServer.Implicits.jsonDecoder[Json])

  def asString(request: Request[Task]): IO[Error, String] =
    asA[String](request)(Http4sServer.Implicits.stringDecoder)
}

object HTTPClient {

  val live: ZManaged[zio.ZEnv, Error, HTTPClient] =
    ZManaged.fromEffect(ZIO.runtime[zio.ZEnv]).flatMap(implicit runtime =>
      BlazeClientBuilder[Task](runtime.platform.executor.asEC)
        .resource.toManagedZIO.map(client =>
        new HTTPClient {
          override def withHTTP4SClient[A](f: Client[Task] => Task[A]): IO[Error, A] = {
            f(client).mapError(HTTPClientError.apply)
          }
        }
      ).mapError(HTTPClientError)
    )
}