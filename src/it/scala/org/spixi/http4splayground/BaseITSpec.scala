package org.spixi.http4splayground

import io.circe.config.parser
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import munit._
import org.flywaydb.core.Flyway
import org.spixi.http4splayground.BaseITSpec.Config
import org.spixi.http4splayground.config.Http4sPlaygroundConfig

import java.util.UUID

trait BaseITSpec extends FunSuite {

  private val config        = parser.decodePath[Http4sPlaygroundConfig]("application").getOrElse(fail(s"Cannot load test"))
  private val dbConf        = config.database
  private val migrationConf = config.flyway

  private val tempDir = s"${Config.baseTempDir}/${UUID.randomUUID.toString}"

  private lazy val pg: EmbeddedPostgres = EmbeddedPostgres
    .builder()
    .setDataDirectory(tempDir)
    .setCleanDataDirectory(true)
    .start()

  private val dbURL = s"jdbc:postgresql://localhost:${pg.getPort}/postgres"

  private lazy val flyway = Flyway
    .configure()
    .dataSource(dbURL, dbConf.user, dbConf.password)
    .sqlMigrationPrefix(migrationConf.prefix)
    .sqlMigrationSeparator(migrationConf.separator)
    .repeatableSqlMigrationPrefix(migrationConf.repeatable)
    .schemas(migrationConf.schemas: _*)
    .table(migrationConf.table)
    .load()

  override def beforeAll(): Unit = {
    super.beforeAll()
    flyway.clean()
    val _ = flyway.migrate()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    pg.close()
  }

}

object BaseITSpec {

  object Config {
    val baseTempDir = "epg-temp-data"
    val port        = 5432
  }
}
