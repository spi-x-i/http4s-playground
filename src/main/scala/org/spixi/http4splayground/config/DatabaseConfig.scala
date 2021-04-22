package org.spixi.http4splayground.config

import cats.syntax.functor._
import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import io.circe.Decoder
import io.circe.generic.semiauto._
import org.slf4j.Logger

import scala.concurrent.ExecutionContext

final case class FlywayConfig(
    path: String,
    prefix: String,
    repeatable: String,
    schemas: List[String],
    separator: String,
    table: String
)

object FlywayConfig {
  implicit val decoder: Decoder[FlywayConfig] = deriveDecoder
}

case class DatabaseConnectionsConfig(poolSize: Int)

object DatabaseConnectionsConfig {
  implicit val decoder: Decoder[DatabaseConnectionsConfig] = deriveDecoder
}

case class DatabaseConfig(
    url: String,
    driver: String,
    user: String,
    password: String,
    schema: String,
    connections: DatabaseConnectionsConfig
)

object DatabaseConfig {

  implicit val decoder: Decoder[DatabaseConfig] = deriveDecoder

  def dbTransactor[F[_]: Async: ContextShift](
      dbc: DatabaseConfig,
      connEc: ExecutionContext,
      blocker: Blocker
  ): Resource[F, HikariTransactor[F]] =
    HikariTransactor
      .newHikariTransactor[F](dbc.driver, dbc.url, dbc.user, dbc.password, connEc, blocker)

  /**
    * Runs the flyway migrations against the target database
    */
  def initializeDb[F[_]](cfg: DatabaseConfig, fc: FlywayConfig)(implicit S: Sync[F], log: Logger): F[Unit] =
    S.delay {
      val fw: Flyway =
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .sqlMigrationPrefix(fc.prefix)
          .sqlMigrationSeparator(fc.separator)
          .repeatableSqlMigrationPrefix(fc.repeatable)
          .schemas(fc.schemas: _*)
          .table(fc.table)
          .load()
      fw.migrate()
      log.info(s"Migration successfully applied on startup")
    }.as(())
}
