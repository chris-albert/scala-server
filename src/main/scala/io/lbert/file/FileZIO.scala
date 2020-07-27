package io.lbert.file

import java.nio.file.{Files, Path}
import java.util.stream.Collectors
import zio.{IO, UIO, UManaged, ZIO, ZManaged}
import scala.io.BufferedSource
import collection.JavaConverters._
import io.lbert.Error
import io.lbert.Error.FileError._

trait FileZIO {

  def cat(path: Path): IO[Error, String]
  def read(path: Path): IO[Error, Array[Byte]]
  def write(path: Path, contents: String): IO[Error, Unit]
  def ls(path: Path, recursive: Boolean = false): IO[Error, List[Path]]
  def exists(path: Path): IO[Error, Boolean]
}

object FileZIO {

  val live: UManaged[FileZIO] = ZManaged.fromEffect(UIO(new FileZIO {
    private def managedSource(path: Path): ZManaged[Any, Error, BufferedSource] =
      ZManaged.make(
        ZIO.effect(scala.io.Source.fromFile(path.toFile))
          .refineOrDie {
            case t => FileCatError(t)
          }
      )(s => UIO(s.close()))

    override def cat(path: Path): IO[Error, String] =
      managedSource(path).use(s =>
        ZIO.effect(s.getLines.mkString("\n"))
          .refineOrDie { case t => FileCatError(t)}
      )

    override def read(path: Path): IO[Error, Array[Byte]] =
      ZIO.effect(Files.readAllBytes(path))
        .refineOrDie { case t => FileCatError(t)}

    override def write(path: Path, contents: String): IO[Error, Unit] =
      ZIO.effect(Files.write(path, contents.getBytes()))
        .mapError(FileWriteError).unit

    override def ls(
      path: Path,
      recursive: Boolean
    ): IO[Error, List[Path]] =
      ZIO.effect(
        (if(recursive) Files.walk(path) else Files.list(path))
          .collect(Collectors.toList()).asScala.toList
      ).refineOrDie {case t => FileLSError(t) }

    override def exists(path: Path): IO[Error, Boolean] =
      IO.effect(Files.exists(path)).mapError(FileExistsError)
  }))

}
