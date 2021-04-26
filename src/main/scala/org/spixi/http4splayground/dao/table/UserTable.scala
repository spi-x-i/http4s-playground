package org.spixi.http4splayground.dao.table

import cats.Monad
import cats.effect.Timer
import cats.syntax.functor._
import org.spixi.http4splayground.models.UserInputDTO

import java.time.Instant
import java.util.UUID
import scala.concurrent.duration.MILLISECONDS

final case class UserTable(
    id: Option[Long] = None,
    uuid: Option[UUID] = None,
    cf: String,
    name: String,
    surname: String,
    age: Int,
    createdAt: java.time.Instant,
    createdBy: String,
    updatedAt: java.time.Instant,
    updatedBy: String,
    deleted: Boolean
)

object UserTable {

  def fromUserInput[F[_]](userIn: UserInputDTO)(implicit T: Timer[F], M: Monad[F]): F[UserTable] = T.clock
    .realTime(MILLISECONDS)
    .map { epoch =>
      val now  = Instant.ofEpochMilli(epoch)
      val user = s"${userIn.name.capitalize} ${userIn.surname.capitalize}"
      UserTable(
        cf = userIn.cf,
        name = userIn.name,
        surname = userIn.surname,
        age = userIn.age,
        createdAt = now,
        createdBy = user,
        updatedAt = now,
        updatedBy = user,
        deleted = false
      )
    }
}
