package org.spixi.http4splayground.services

import cats.effect._
import cats.syntax.all._
import org.slf4j.Logger
import org.spixi.http4splayground.dao.UserDAO
import org.spixi.http4splayground.dao.table.UserTable
import org.spixi.http4splayground.models.{UserInputDTO, UserOutputDTO}
import cats.Monad

trait UserService[F[_]] {
  def create(userIn: UserInputDTO): F[UserOutputDTO]
}

object UserService {
  implicit def apply[F[_]](implicit ev: UserService[F]): UserService[F] = ev

  def impl[F[_]](userDAO: UserDAO[F])(implicit
      log: Logger,
      T: Timer[F],
      M: Monad[F]
  ): UserService[F] =
    new UserService[F] {

      def create(userIn: UserInputDTO): F[UserOutputDTO] = {
        log.info(s"User creation request: user: $userIn.")
        for {
          userToCreate <- UserTable.fromUserInput(userIn)
          userCreated  <- userDAO.create(userToCreate)
          result       <- UserOutputDTO.fromUser(userCreated)
        } yield result
      }
    }
}
