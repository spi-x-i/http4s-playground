package org.spixi.http4splayground.controller

import cats.effect.IO
import munit.CatsEffectSuite
import org.spixi.http4splayground.BaseITSpec

class UserControllerITSpec extends CatsEffectSuite with BaseITSpec {

  test("placeholder") {
    assertIO(IO.pure(1), 1)
  }

}
