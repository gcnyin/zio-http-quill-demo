package example

final case class ErrorMsg(code: String, message: String)

object ErrorMsg {
  import zio.json._
  implicit val encoder: JsonEncoder[ErrorMsg] = DeriveJsonEncoder.gen[ErrorMsg]

  def invalidRequest(message: String): ErrorMsg = ErrorMsg("INVALID_REQUEST", message)

  def invalidBody(message: String): ErrorMsg = ErrorMsg("INVALID_BODY", message)

  def internalError(message: String): ErrorMsg = ErrorMsg("INTERNAL_ERROR", message)
}
