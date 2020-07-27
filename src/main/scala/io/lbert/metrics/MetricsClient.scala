package io.lbert.metrics

import io.lbert.config.MetricsConfig
import io.lbert.log.Logger
import io.lbert.metrics.Metrics.Names
import zio.clock.Clock
import zio.{Queue, UIO, UManaged, ZLayer, ZManaged}
import zio.metrics.{Metric, Tag}
import zio.metrics.dogstatsd.DogStatsDClient
import zio.metrics.encoders.Encoder

case class MetricsClient(
  client    : DogStatsDClient,
  tags      : Seq[Tag],
  prefix    : Names,
  sampleRate: Double,
  queue     : Queue[Metric],
  async     : Boolean
)

object MetricsClient {

  def live(
    config: MetricsConfig,
    logger: Logger,
    clock: Clock.Service
  ): UManaged[MetricsClient] = {
    val dogClient = DogStatsDClient(
      config.bufferSize.size,
      config.timeout.timeout * 1000000,
      config.queueSize.size,
      Some(config.host.host),
      Some(config.port.port)
    )
    ZManaged.fromEffect(for {
      queue <- dogClient.queue
      _     <- logger.info(s"Metrics are enabled!!! Sending to [${config.host.host}]").when(config.enabled.enabled)
      _     <- logger.info(s"Metrics disabled!!!").when(!config.enabled.enabled)
      _     <- dogClient.listen(queue).provideLayer(ZLayer.succeed(clock) ++ Encoder.dogstatsd)
        .when(config.enabled.enabled)
    } yield MetricsClient(
      dogClient,
      Seq(
        Tag("environment", ""),
        Tag("service", "")
      ),
      Names(config.prefix.prefix.split("\\."):_*),
      config.sampleRate.sampleRate,
      queue,
      config.async.async
    ))
  }
}
