package io.lbert.metrics

import io.lbert.config.MetricsConfig
import io.lbert.log.Logger
import io.lbert.metrics.Metrics.Names
import zio.clock.Clock
import zio.duration.Duration
import zio.{UIO, UManaged, ZIO, ZManaged}

trait Metrics {

  def timeWithElapsed[R, E, A](msg: Names)(zio: ZIO[R, E, A]): ZIO[R, E, (A, Long)]
  def mark(names: Names): UIO[Unit]
  def update(names: Names, value: Double): UIO[Unit]
  def counter(names: Names, value: Double): UIO[Unit]
  def increment(names: Names): UIO[Unit]
  def decrement(names: Names): UIO[Unit]

  def time[R, E, A](names: Names)(zio: ZIO[R, E, A]): ZIO[R, E, A] =
    timeWithElapsed(names)(zio).map(_._1)
}

object Metrics {

  case class Names(names: String*) {self =>
    def formattedName: String = names.mkString(".")
    def formattedName(names: Names): String =
      Names((names.names ++ self.names):_*).formattedName
  }

  def statsD(
    config: MetricsConfig,
    logger: Logger,
    clock: Clock.Service
  ): UManaged[Metrics] =
    MetricsClient.live(config, logger, clock)
      .flatMap(fromClient(_, clock))

  def fromClient(
    metricsClient: MetricsClient,
    clock: Clock.Service
  ): UManaged[Metrics] = ZManaged.fromEffect(UIO(new Metrics {
    override def timeWithElapsed[R, E, A](names: Names)
      (zio: ZIO[R, E, A]): ZIO[R, E, (A, Long)] =
      for {
        start <- clock.nanoTime
        a     <- zio
        end   <- clock.nanoTime
        diff   = end - start
        _     <- metricsClient.client.timer(names.formattedName(metricsClient.prefix), diff / 1000000, metricsClient.sampleRate, metricsClient.tags)(metricsClient.queue)
          .orDie
      } yield (a, diff)

    override def mark(names: Names): UIO[Unit] =
      metricsClient.client.meter(names.formattedName(metricsClient.prefix), 1, metricsClient.tags, metricsClient.async)(metricsClient.queue).orDie

    override def update(
      names: Names,
      value: Double
    ): UIO[Unit] =
      metricsClient.client.gauge(names.formattedName(metricsClient.prefix), value, metricsClient.tags, metricsClient.async)(metricsClient.queue).orDie

    override def counter(
      names: Names,
      value: Double
    ): UIO[Unit] =
      metricsClient.client.counter(names.formattedName(metricsClient.prefix), value, metricsClient.sampleRate, metricsClient.tags, metricsClient.async)(metricsClient.queue).orDie

    override def increment(names: Names): UIO[Unit] =
      metricsClient.client.increment(names.formattedName(metricsClient.prefix), metricsClient.sampleRate, metricsClient.tags, metricsClient.async)(metricsClient.queue).orDie

    override def decrement(names: Names): UIO[Unit] =
      metricsClient.client.decrement(names.formattedName(metricsClient.prefix), metricsClient.sampleRate, metricsClient.tags, metricsClient.async)(metricsClient.queue).orDie
  }))

  def logger(
    logger: Logger,
    clock: Clock.Service
  ): UManaged[Metrics] = ZManaged.fromEffect(UIO(new Metrics {

    override def timeWithElapsed[R, E, A](names: Names)
      (zio: ZIO[R, E, A]): ZIO[R, E, (A, Long)] =
      for {
        start <- clock.nanoTime
        a     <- zio
        end   <- clock.nanoTime
        diffNs = end - start
        dur    = Duration.fromNanos(diffNs)
        _ <- logger.info(s"time - ${names.formattedName} [${dur.render}]")
      } yield (a, diffNs)

    override def mark(names: Names): UIO[Unit] =
      logger.info(s"mark - ${names.formattedName}")

    override def update(
      names: Names,
      value: Double
    ): UIO[Unit] =
      logger.info(s"update - ${names.formattedName} [$value]")

    override def counter(
      names: Names,
      value: Double
    ): UIO[Unit] =
      logger.info(s"counter - ${names.formattedName} [$value]")

    override def increment(names: Names): UIO[Unit] =
      logger.info(s"increment - ${names.formattedName}")

    override def decrement(names: Names): UIO[Unit] =
      logger.info(s"increment - ${names.formattedName}")
  }))
}
