package io.lbert.config

import zio.config.ConfigDescriptor
import ConfigDescriptor._
import com.typesafe.config.ConfigFactory
import io.lbert.Error.ConfigError
import io.lbert.Error
import zio.ZManaged
import zio.config.typesafe.TypesafeConfig

final case class AppConfig(
  server: ServerConfig,
  metrics: MetricsConfig
)

object AppConfig {

  val CONFIG_FILENAME = "application.conf"

  val config: ConfigDescriptor[AppConfig] = (
    nested("server")(ServerConfig.config) |@|
    nested("metrics")(MetricsConfig.config)
    )(AppConfig.apply, AppConfig.unapply)

  val live: ZManaged[Any, Error, AppConfig] =
    TypesafeConfig.fromTypesafeConfig(
      ConfigFactory.parseResources(CONFIG_FILENAME).resolve(), config
    ).build.bimap(ConfigError, _.get)
}