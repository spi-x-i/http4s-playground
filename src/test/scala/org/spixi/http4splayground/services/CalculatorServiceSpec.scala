package org.spixi.http4splayground.services

import cats.effect._
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.spixi.http4splayground.Routes
import org.spixi.http4splayground.mock.SumClientMock
import org.spixi.http4splayground.services.CalculatorService.Sum

class CalculatorServiceSpec extends CatsEffectSuite with SumClientMock {

  test("Sum service returns sum with both members") {
    assertIO(sumRequest(Some(10), Some(10)).flatMap(_.as[Sum]), Sum(20))
  }

  test("Sum service returns status code 200 on sum ") {
    assertIO(sumRequest(Some(10), Some(10)).map(_.status), Status.Ok)
  }

  private[this] def sumRequest(no1: Option[Int], no2: Option[Int]): IO[Response[IO]] = {
    val queryParams = (no1, no2) match {
      case (Some(no1), Some(no2)) => s"?add1=$no1&add2=$no2"
      case (Some(no1), _)         => s"?add1=$no1"
      case (_, Some(no2))         => s"?add2=$no2"
      case _                      => ""
    }

    val getHW      = Request[IO](Method.GET, Uri.unsafeFromString(s"/sum$queryParams"))
    val helloWorld = CalculatorService.impl[IO](sumClientMock)
    Routes.sumControllerRoutes(helloWorld).orNotFound(getHW)
  }

}
