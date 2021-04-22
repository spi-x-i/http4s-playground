package org.spixi.http4splayground.config

import io.circe.Decoder
import io.circe.generic.semiauto._

final case class ApplicationConfig(host: String, port: Int)

object ApplicationConfig {
  implicit val decoder: Decoder[ApplicationConfig] = deriveDecoder
}

final case class Http4sPlaygroundConfig(database: DatabaseConfig, server: ApplicationConfig, flyway: FlywayConfig)

object Http4sPlaygroundConfig {
  implicit val decoder: Decoder[Http4sPlaygroundConfig] = deriveDecoder
}
