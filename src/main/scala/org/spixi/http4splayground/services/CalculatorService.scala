package org.spixi.http4splayground.services

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe._
import org.spixi.http4splayground.clients.SumClient

trait CalculatorService[F[_]] {
  def sum(no1: Int, no2: Int): F[CalculatorService.Sum]
}

object CalculatorService {
  implicit def apply[F[_]](implicit ev: CalculatorService[F]): CalculatorService[F] = ev

  final case class Name(name: String) extends AnyVal

  final case class Sum(result: Int) extends AnyVal

  object Sum {
    implicit val sumEncoder: Encoder[Sum]                                    = deriveEncoder[Sum]
    implicit def jokeEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Sum] = jsonEncoderOf
    implicit val sumDecoder: Decoder[Sum]                                    = deriveDecoder[Sum]
    implicit def jokeEntityDecoder[F[_]: Sync]: EntityDecoder[F, Sum]        = jsonOf
  }

  def impl[F[_]: Applicative](client: SumClient[F]): CalculatorService[F] = new CalculatorService[F] {

    def sum(no1: Int, no2: Int): F[Sum] = {
      println(s"Request for a sum: [$no1 + $no2].")
      client.sum(no1, no2).map(Sum.apply)
    }
  }
}
