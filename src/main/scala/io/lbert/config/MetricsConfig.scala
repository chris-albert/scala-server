package io.lbert.config

import zio.config._
import ConfigDescriptor._

case class MetricsConfig(
  host      : MetricsConfig.Host,
  port      : MetricsConfig.Port,
  prefix    : MetricsConfig.Prefix,
  bufferSize: MetricsConfig.BufferSize,
  timeout   : MetricsConfig.Timeout,
  queueSize : MetricsConfig.QueueSize,
  sampleRate: MetricsConfig.SampleRate,
  async     : MetricsConfig.Async,
  enabled   : MetricsConfig.Enabled,
  tags      : MetricsConfig.Tags
)

object MetricsConfig {

  final case class Host(host: String)
  final case class Port(port: Int)
  final case class Prefix(prefix: String)
  final case class BufferSize(size: Long)
  final case class Timeout(timeout: Long)
  final case class QueueSize(size: Int)
  final case class SampleRate(sampleRate: Double)
  final case class Async(async: Boolean)
  final case class Enabled(enabled: Boolean)
  final case class Tags(tags: Map[String, String])

  val config = (
    string("host").xmap[Host](Host.apply, _.host) |@|
    int("port").xmap[Port](Port.apply, _.port) |@|
    string("prefix").xmap[Prefix](Prefix.apply, _.prefix) |@|
    long("bufferSize").xmap[BufferSize](BufferSize.apply, _.size) |@|
    long("timeout").xmap[Timeout](Timeout.apply, _.timeout) |@|
    int("queueSize").xmap[QueueSize](QueueSize.apply, _.size) |@|
    double("sampleRate").xmap[SampleRate](SampleRate.apply, _.sampleRate) |@|
    boolean("async").xmap[Async](Async.apply, _.async) |@|
    boolean("enabled").xmap[Enabled](Enabled.apply, _.enabled) |@|
    map[String]("tags")(string).xmap[Tags](Tags.apply, _.tags)
  )(MetricsConfig.apply, MetricsConfig.unapply)
}
