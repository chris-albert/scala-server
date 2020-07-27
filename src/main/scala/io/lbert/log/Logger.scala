package io.lbert.log

import io.circe.{Json, Printer}
import org.slf4j.{LoggerFactory, Logger => Slf4jLogger}
import zio.{UIO, UManaged, ZManaged}

trait Logger {
  def trace(msg: String): UIO[Unit]
  def debug(msg: String): UIO[Unit]
  def info(msg: String): UIO[Unit]
  def warn(msg: String): UIO[Unit]
  def error(msg: String): UIO[Unit]

  def trace(json: Json): UIO[Unit] = trace(json.printWith(Printer.noSpaces))
  def debug(json: Json): UIO[Unit] = debug(json.printWith(Printer.noSpaces))
  def info(json: Json): UIO[Unit] = info(json.printWith(Printer.noSpaces))
  def warn(json: Json): UIO[Unit] = warn(json.printWith(Printer.noSpaces))
  def error(json: Json): UIO[Unit] = error(json.printWith(Printer.noSpaces))
}

object Logger {

  def ofName(name: String): UManaged[Logger] = ZManaged.fromEffect(
    UIO.effectTotal(slf4j(LoggerFactory.getLogger(name)))
  )

  def slf4j(slf4jLogger: Slf4jLogger): Logger = new Logger {
    override def trace(msg: String): UIO[Unit] = UIO(slf4jLogger.trace(msg))
    override def debug(msg: String): UIO[Unit] = UIO(slf4jLogger.debug(msg))
    override def info(msg: String): UIO[Unit]  = UIO(slf4jLogger.info(msg))
    override def warn(msg: String): UIO[Unit]  = UIO(slf4jLogger.warn(msg))
    override def error(msg: String): UIO[Unit] = UIO(slf4jLogger.error(msg))
  }
}
