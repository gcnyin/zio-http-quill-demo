package example.repository

import example.model.User
import zio.ZIO

import java.sql.SQLException

trait UserRepository {
  def listUser: ZIO[Any, SQLException, Seq[(Int, String)]]

  def createUser(username: String, password: String): ZIO[Any, SQLException, Int]
}
