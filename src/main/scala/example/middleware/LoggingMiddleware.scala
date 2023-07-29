package example.middleware

import zio.Trace
import zio.http._
import zio.logging.LogAnnotation

import java.util.UUID

class LoggingMiddleware extends RequestHandlerMiddleware.Simple[Any, Nothing] {
  private val urlPathAnnotation: LogAnnotation[String] = LogAnnotation[String](
    name = "url_path",
    combine = (_, r) => r,
    render = identity
  )

  override def apply[R1 <: Any, Err1 >: Nothing](
      handler: Handler[R1, Err1, Request, Response]
  )(implicit trace: Trace): Handler[R1, Err1, Request, Response] =
    Handler.fromFunctionZIO[Request] { request =>
      handler.runZIO(request) @@ LogAnnotation.TraceId(UUID.randomUUID()) @@ urlPathAnnotation(request.path.toString())
    }
}
