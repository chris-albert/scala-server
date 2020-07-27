package io.lbert

sealed trait Error

object Error {

  final case class ConfigError(error: Throwable) extends Error
  final case class HTTPClientError(error: Throwable) extends Error

  sealed trait FileError extends Error

  object FileError {

    final case class FileCatError(t: Throwable) extends FileError
    final case class FileWriteError(t: Throwable) extends FileError
    final case class FileExistsError(t: Throwable) extends FileError
    final case class FileLSError(t: Throwable) extends FileError
  }
}
