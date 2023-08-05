package example.middleware

import zio.Trace
import zio.http._
import zio.logging.LogAnnotation
import zio.metrics.MetricKeyType.Counter
import zio.metrics.{Metric, MetricState}

import java.util.UUID

class LoggingMiddleware extends RequestHandlerMiddleware.Simple[Any, Nothing] {
  private val urlPathAnnotation: LogAnnotation[String] = LogAnnotation[String](
    name = "url_path",
    combine = (_, r) => r,
    render = identity
  )

  private val countRequests: Metric[Counter, Any, MetricState.Counter] = Metric.counter("countRequests").contramap[Any](_ => 1L)

  override def apply[R1 <: Any, Err1 >: Nothing](
      handler: Handler[R1, Err1, Request, Response]
  )(implicit trace: Trace): Handler[R1, Err1, Request, Response] =
    Handler.fromFunctionZIO[Request] { request =>
      handler.runZIO(request) @@
        LogAnnotation.TraceId(UUID.randomUUID()) @@
        urlPathAnnotation(request.path.toString()) @@
        countRequests.tagged("path", request.path.toString())
    }
}
