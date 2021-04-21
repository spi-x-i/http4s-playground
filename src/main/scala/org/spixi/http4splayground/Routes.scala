package org.spixi.http4splayground

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.spixi.http4splayground.services.CalculatorService

object Routes {

  object OptionalAddend1Matcher extends OptionalQueryParamDecoderMatcher[Int]("add1")
  object OptionalAddend2Matcher extends OptionalQueryParamDecoderMatcher[Int]("add2")

  def sumControllerRoutes[F[_]: Sync](H: CalculatorService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] { case GET -> Root / "sum" :? OptionalAddend1Matcher(add1) +& OptionalAddend2Matcher(add2) =>
      for {
        result   <- H.sum(add1.getOrElse(0), add2.getOrElse(0))
        response <- Ok(result)
      } yield response
    }
  }
}
