package example.middleware

import zio.http._
import zio.logging.LogAnnotation
import zio.{Trace, ZIO}

import java.util.UUID

class LoggingMiddleware extends RequestHandlerMiddleware.Simple[Any, Nothing] {
  override def apply[R1 <: Any, Err1 >: Nothing](
      handler: Handler[R1, Err1, Request, Response]
  )(implicit trace: Trace): Handler[R1, Err1, Request, Response] =
    Handler.fromFunctionZIO[Request] { request =>
      val h = for {
        _ <- ZIO.log(request.url.path.toString())
        response <- handler.runZIO(request)
      } yield response
      h @@ LogAnnotation.TraceId(UUID.randomUUID())
    }
}
