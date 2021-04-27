package org.spixi.http4splayground.models

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.spixi.http4splayground.dao.table.UserTable

import java.util.UUID

trait UserDTO {}

final case class UserInputDTO(
    cf: String,
    name: String,
    surname: String,
    age: Int
)

object UserInputDTO {
  implicit val userInEncoder: Encoder[UserInputDTO]                                   = deriveEncoder[UserInputDTO]
  implicit def userInEntityEncoder[F[_]: Applicative]: EntityEncoder[F, UserInputDTO] = jsonEncoderOf
  implicit val userDecoder: Decoder[UserInputDTO]                                     = deriveDecoder[UserInputDTO]
  implicit def userInEntityDecoder[F[_]: Sync]: EntityDecoder[F, UserInputDTO]        = jsonOf
}

final case class UserOutputDTO(
    uuid: UUID,
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

object UserOutputDTO {
  implicit val userOutEncoder: Encoder[UserOutputDTO]                                   = deriveEncoder[UserOutputDTO]
  implicit def userOutEntityEncoder[F[_]: Applicative]: EntityEncoder[F, UserOutputDTO] = jsonEncoderOf
  implicit val userDecoder: Decoder[UserOutputDTO]                                      = deriveDecoder[UserOutputDTO]
  implicit def userOutEntityDecoder[F[_]: Sync]: EntityDecoder[F, UserOutputDTO]        = jsonOf

  @throws[IllegalStateException]
  def fromUser[F[_]: Applicative](user: UserTable): F[UserOutputDTO] =
    UserOutputDTO(
      uuid = user.uuid.getOrElse(throw new IllegalStateException(s"Without uuid cannot create user.")),
      cf = user.cf,
      name = user.name,
      surname = user.surname,
      age = user.age,
      createdAt = user.createdAt,
      createdBy = user.createdBy,
      updatedAt = user.updatedAt,
      updatedBy = user.updatedBy,
      deleted = false
    ).pure[F]
}
