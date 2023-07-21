package example.request

import zio.json._

final case class CreateUserRequest(username: String, password: String)

object CreateUserRequest {
  implicit val decoder: JsonDecoder[CreateUserRequest] = DeriveJsonDecoder.gen[CreateUserRequest]
}
