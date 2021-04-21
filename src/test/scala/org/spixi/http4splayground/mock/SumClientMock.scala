package org.spixi.http4splayground.mock

import cats.implicits._
import cats.effect._
import org.spixi.http4splayground.clients.SumClient

import scala.concurrent.ExecutionContext

trait SumClientMock {

  val EC: ExecutionContext = ExecutionContext.global

  implicit val cs: ContextShift[IO] = IO.contextShift(EC)

  def sumClientMock: SumClient[IO] =
    new SumClient[IO] {
      override def sum(no1: Int, no2: Int): IO[Int] = (no1 + no2).pure[IO]
    }

}
