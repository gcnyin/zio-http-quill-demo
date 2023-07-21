package example.model

import zio.json._

final case class User(userId: Int, username: String, password: String)

object User {
  implicit val encoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]
}
