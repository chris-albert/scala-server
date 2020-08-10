package io.lbert.server

import org.http4s.HttpRoutes
import zio.Task
import zio.interop.catz._
import io.lbert.server.Http4sServer.Implicits._

object Health {

  import http4sDsl._

  val routes: HttpRoutes[Task] = HttpRoutes.of[Task] {
    case GET -> Root / "health" =>
      Ok("OK")
  }
}
