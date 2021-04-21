package org.spixi.http4splayground

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.spixi.http4splayground.clients.SumClient
import org.spixi.http4splayground.services.CalculatorService

import scala.concurrent.ExecutionContext.global

object Server {

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], cs: ContextShift[F]): Stream[F, Nothing] = {
    for {
      // clients
      _ <- BlazeClientBuilder[F](global).stream
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
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
