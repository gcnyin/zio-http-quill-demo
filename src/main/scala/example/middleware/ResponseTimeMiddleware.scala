package example.middleware

import zio.http._
import zio.{Trace, ZIO}

class ResponseTimeMiddleware extends RequestHandlerMiddleware.Simple[Any, Nothing] {
  override def apply[R1 <: Any, Err1 >: Nothing](
      handler: Handler[R1, Err1, Request, Response]
  )(implicit trace: Trace): Handler[R1, Err1, Request, Response] =
    Handler.fromFunctionZIO[Request] { request =>
      for {
        startTime <- ZIO.succeed(System.currentTimeMillis())
        response <- handler.runZIO(request)
        endTime <- ZIO.succeed(System.currentTimeMillis())
      } yield response.addHeader(Header.Custom("X-Response-Time", s"${endTime - startTime}"))
    }
}
