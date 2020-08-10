package io.lbert.server

import io.lbert.file.FileZIO
import zio.interop.catz._
import io.lbert.server.Http4sServer.Implicits._
import org.http4s.{Header, HttpRoutes}
import zio.{Task, ZManaged}
import java.nio.file.{Paths, Path => JavaPath}

object Static {

  import http4sDsl._

  val contentMappings = Map(
    "html" -> "text/html",
    "js"   -> "text/javascript",
    "css"  -> "text/css",
    "bmp"  -> "image/bmp",
    "gif"  -> "image/gif",
    "ico"  -> "image/x-icon",
    "jpeg" -> "image/jpeg",
    "png"  -> "image/png",
    "tif"  -> "image/tiff"
  )

  def routes(rootPath: JavaPath): ZManaged[FileZIO, Nothing, HttpRoutes[Task]] =
    ZManaged.environment[FileZIO].map(file =>
      HttpRoutes.of[Task] {
        case GET -> rest =>
          val path = toJavaPath(rest)
          file.read(rootPath.resolve(path))
          .either.flatMap {
            case Left(value)  => BadRequest(value.toString)
            case Right(value) => Ok(new String(value)).map(_.withHeaders(getContentType(path)))
          }
      }
    )

  def toJavaPath(inPath: Path): JavaPath = {
    val path = Paths.get(inPath.toList.mkString("/"))
    if(path.getFileName.toString.contains(".")) path else path.resolve("index.html")
  }

  def getContentType(path: JavaPath): Header = {
    val contentType = path.getFileName.toString.split("\\.").lastOption
      .flatMap(contentMappings.get)
      .getOrElse("text/plain")
    Header("Content-Type", contentType)
  }
}
