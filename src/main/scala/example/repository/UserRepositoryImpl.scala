package example.repository

import example.model.User
import io.getquill.CompositeNamingStrategy
import io.getquill.jdbczio.Quill
import zio.{ZIO, ZLayer}

import java.sql.SQLException

final case class UserRepositoryImpl(quill: Quill.Postgres[CompositeNamingStrategy]) extends UserRepository {

  import quill._

  override def listUser: ZIO[Any, SQLException, Seq[User]] = run(query[User])

  override def createUser(username: String, password: String): ZIO[Any, SQLException, Int] =
    run(quote(query[User].insertValue(lift(User(userId = 0, username = username, password = password)))).returningGenerated(_.userId))
}

object UserRepositoryImpl {
  val layer: ZLayer[Quill.Postgres[CompositeNamingStrategy], Nothing, UserRepositoryImpl] = ZLayer.fromFunction(UserRepositoryImpl.apply _)
}