package example

final case class ErrorMsg(code: String, message: String)

object ErrorMsg {
  import zio.json._
  implicit val encoder: JsonEncoder[ErrorMsg] = DeriveJsonEncoder.gen[ErrorMsg]
}