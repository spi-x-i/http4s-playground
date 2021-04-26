package org.spixi.http4splayground.dao

import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import org.spixi.http4splayground.dao.table.UserTable
import java.util.UUID

private object UserSQL {
  import doobie.implicits.legacy.instant._
  // we need this custom mapping because uuid not supported out-of-the-box
  implicit val uuidMeta: Meta[UUID] = Meta[String].timap(UUID.fromString)(_.toString)

  def insert(user: UserTable): Update0 = sql"""
    INSERT INTO "playground"."USER" ("CF", "NAME", "SURNAME", "AGE", "CREATED_AT", "CREATED_BY", "LAST_UPDATED_AT", "LAST_UPDATED_BY", "DELETED")
    VALUES (${user.cf}, ${user.name}, ${user.surname}, ${user.age}, ${user.createdAt}, ${user.createdBy}, ${user.updatedAt}, ${user.updatedBy}, ${user.deleted})
  """.update

}

trait UserDAO[F[_]] {
  def create(user: UserTable): F[UserTable]
}

object UserDAO {

  def impl[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): UserDAO[F] =
    new UserDAO[F] {
      import UserSQL._

      def create(user: UserTable): F[UserTable] =
        insert(user)
          .withUniqueGeneratedKeys[(Long, UUID)]("ID", "UUID")
          .map({ case (id, uuid) => user.copy(id = id.some, uuid = uuid.some) })
          .transact(xa)
    }
}
