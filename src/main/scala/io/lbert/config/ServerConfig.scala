package io.lbert.config

import zio.config.ConfigDescriptor
import ConfigDescriptor._

final case class ServerConfig(
  bind: ServerConfig.Bind,
  port: ServerConfig.Port
)

object ServerConfig {
  final case class Bind(bind: String)
  final case class Port(port: Short)

  val config: ConfigDescriptor[ServerConfig] = (
    string("bind").xmap[Bind](Bind.apply, _.bind) |@|
    short("port").xmap[Port](Port.apply, _.port)
  )(ServerConfig.apply, ServerConfig.unapply)
}
