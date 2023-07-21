package example.repository

import example.model.User
import io.getquill.jdbczio.Quill
import io.getquill.{CompositeNamingStrategy2, PostgresEscape, SnakeCase}
import zio.{ZIO, ZLayer}

import java.sql.SQLException

final case class UserRepositoryImpl(quill: Quill.Postgres[CompositeNamingStrategy2[SnakeCase, PostgresEscape]]) extends UserRepository {

  import quill._

  override def listUser: ZIO[Any, SQLException, Seq[User]] = run(query[User])

  override def createUser(username: String, password: String): ZIO[Any, SQLException, Int] =
    run(quote(query[User].insertValue(lift(User(userId = 0, username = username, password = password)))).returningGenerated(_.userId))
}

object UserRepositoryImpl {
  val layer: ZLayer[Quill.Postgres[CompositeNamingStrategy2[SnakeCase, PostgresEscape]], Nothing, UserRepositoryImpl] =
    ZLayer.fromFunction(UserRepositoryImpl.apply _)
}
