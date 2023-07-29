package example.response

import zio.json._

final case class CreateUserResponse(userId: Int)

object CreateUserResponse {
  implicit val encoder: JsonEncoder[CreateUserResponse] = DeriveJsonEncoder.gen[CreateUserResponse]
}
