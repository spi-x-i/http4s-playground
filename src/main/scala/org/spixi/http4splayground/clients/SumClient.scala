package org.spixi.http4splayground.clients

import cats.effect.{ConcurrentEffect, ContextShift, Resource}
import cats.implicits._
import greet.sum.{Addends, SumRequest, SumService}
import higherkindness.mu.rpc._

trait SumClient[F[_]] {
  def sum(no1: Int, no2: Int): F[Int]
}

object SumClient {
  def apply[F[_]](implicit ev: SumClient[F]): SumClient[F] = ev

  val channelFor: ChannelFor = ChannelForAddress("localhost", 50051)

  def clientResource[F[_]: ConcurrentEffect: ContextShift]: Resource[F, SumService[F]] =
    SumService.client[F](channelFor)

  def impl[F[_]: ConcurrentEffect](implicit cs: ContextShift[F]): SumClient[F] =
    new SumClient[F] {
      override def sum(no1: Int, no2: Int): F[Int] = run(no1, no2)
    }

  private def run[F[_]: ConcurrentEffect: ContextShift](add1: Int, add2: Int): F[Int] = {
    val body = SumRequest(Some(Addends(add1, add2)))
    for {
      response <- clientResource.use(client => client.Sum(body))
      _ = println(
        s"Response to sum request $add1 + $add2:\t [${response.result}]."
      )
    } yield response.result
  }
}
