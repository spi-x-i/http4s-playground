package org.spixi.http4splayground.services

import cats.effect._
import cats.implicits._
import munit.CatsEffectSuite
import org.mockito._
import org.slf4j.{Logger, LoggerFactory}
import org.spixi.http4splayground.dao.UserDAO
import org.spixi.http4splayground.dao.table.UserTable
import org.spixi.http4splayground.models.{UserInputDTO, UserOutputDTO}

import java.time.Instant
import java.util.UUID
import scala.concurrent.duration.MILLISECONDS

class UserServiceSpec extends CatsEffectSuite with IdiomaticMockito {

  implicit val log: Logger = LoggerFactory.getLogger(this.getClass)

  private val timerStub = mock[Timer[IO]]
  private val clockMock = mock[Clock[IO]]
  private val now       = Instant.ofEpochMilli(System.currentTimeMillis())
  clockMock.realTime(MILLISECONDS) returns IO.pure(now.toEpochMilli)
  timerStub.clock returns clockMock
  private val uuid = UUID.randomUUID()

  private val userDAO = new UserDAO[IO] {
    override def create(user: UserTable): IO[UserTable] = IO.pure(user.copy(id = 1L.some, uuid = uuid.some))
  }
  private val serviceMock = UserService.impl(userDAO)(log, timerStub)

  test("create a user correctly") {
    val probe = UserInputDTO("CF", "spixi", "test", 32)
    val ret   = serviceMock.create(probe)
    val expected = UserOutputDTO(
      uuid,
      probe.cf,
      probe.name,
      probe.surname,
      probe.age,
      now,
      "Spixi Test",
      now,
      "Spixi Test",
      deleted = false
    )
    assertIO(ret, expected)
  }

}
