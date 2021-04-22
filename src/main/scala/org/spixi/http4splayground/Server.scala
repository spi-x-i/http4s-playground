package org.spixi.http4splayground

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.implicits._
import doobie.util.ExecutionContexts
import fs2.Stream
import io.circe.config.parser
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{Logger => MLogger}
import org.slf4j.{Logger, LoggerFactory}
import org.spixi.http4splayground.clients.SumClient
import org.spixi.http4splayground.config.{DatabaseConfig, Http4sPlaygroundConfig}
import org.spixi.http4splayground.services.CalculatorService

import scala.concurrent.duration._

object Server {

  implicit val log: Logger = LoggerFactory.getLogger(this.getClass)

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], cs: ContextShift[F]): Stream[F, Nothing] = {
    for {
      // logger and configs
      conf <- Stream.eval(parser.decodePathF[F, Http4sPlaygroundConfig]("application"))
      // execution contexts
      serverEc <- Stream.resource(ExecutionContexts.cachedThreadPool[F])
      clientEc <- Stream.resource(ExecutionContexts.cachedThreadPool[F])
      // connEc <- Stream.resource(ExecutionContexts.fixedThreadPool[F](conf.database.connections.poolSize))
      // txnEc <- Stream.resource(ExecutionContexts.cachedThreadPool[F])
      // clients
      _ <- BlazeClientBuilder[F](clientEc).stream
      sumClient = SumClient.impl[F]

      // services
      sumSerivceImpl = CalculatorService.impl[F](sumClient)
      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = (
        Routes.sumControllerRoutes[F](sumSerivceImpl)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = MLogger.httpApp(logHeaders = true, logBody = true)(httpApp)
      migrationF   = DatabaseConfig.initializeDb(conf.database, conf.flyway)
      _ <- Stream
        .retry(migrationF, 1.second, nextDelay = _ * 2, 5)
        .handleError { e => log.error(s"Failed to provide migration init.", e); () }
      exitCode <- BlazeServerBuilder[F](serverEc)
        .bindHttp(conf.server.port, conf.server.host)
        .withHttpApp(finalHttpApp)
        .stream
    } yield exitCode
  }.drain
}
