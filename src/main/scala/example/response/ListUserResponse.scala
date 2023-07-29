package example.response

import zio.json._

final case class ListUserResponse(users: Seq[ListUserResponse.User])

object ListUserResponse {
  implicit val encoder: JsonEncoder[ListUserResponse] = DeriveJsonEncoder.gen[ListUserResponse]

  final case class User(userId: Int, username: String)
  object User {
    implicit val encoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]
  }
}
