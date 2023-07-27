package example

import at.favre.lib.crypto.bcrypt.BCrypt
import example.middleware.{LoggingMiddleware, ResponseTimeMiddleware}
import example.repository.{UserRepository, UserRepositoryImpl}
import example.request.CreateUserRequest
import example.response.ListUserResponse
import io.getquill.jdbczio.Quill
import io.getquill.{CompositeNamingStrategy2, PostgresEscape, SnakeCase}
import zio.http._
import zio.json._
import zio.logging.backend.SLF4J
import zio.{Runtime, Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

import java.io.StringWriter
import javax.sql.DataSource

object Main extends ZIOAppDefault {
  def app: ZIO[UserRepository, Any, Http[Any, Nothing, Request, Response]] = {
    ZIO.environment[UserRepository].map { env =>
      val userRepository = env.get[UserRepository]
      httpApp(userRepository)
    }
  }

  def httpApp(userRepository: UserRepository): Http[Any, Nothing, Request, Response] = {
    Http.collectZIO[Request] {
      case Method.GET -> Root / "user" / "list" =>
        val response = for {
          users <- userRepository.listUser.mapError(e => ErrorMsg("INTERNAL_ERROR", e.getMessage))
          _ <- ZIO.log(s"find ${users.size} users")
          response = ListUserResponse(users.map { user => ListUserResponse.User(user.userId, user.username) })
        } yield Response.json(response.toJson)
        response.catchAll(errorMsg => ZIO.succeed(Response.json(errorMsg.toJson)))

      case req @ Method.POST -> Root / "user" =>
        val response = for {
          rawBody <- req.body.asString.mapError(e => ErrorMsg("INVALID_REQUEST", e.getMessage))
          request <- ZIO.fromEither(rawBody.fromJson[CreateUserRequest]).mapError(e => ErrorMsg("INVALID_BODY", e))
          password <- ZIO.succeed(BCrypt.withDefaults().hashToString(12, request.password.toCharArray))
          index <- userRepository
            .createUser(username = request.username, password = password)
            .mapError(e => ErrorMsg("INTERNAL_ERROR", e.getMessage))
        } yield Response.text(index.toString)
        response.catchAll(errorMsg => ZIO.succeed(Response.json(errorMsg.toJson)))

      case Method.GET -> Root / "hikaricp-metrics" =>
        import io.prometheus.client.CollectorRegistry
        import io.prometheus.client.exporter.common.TextFormat

        val writer = new StringWriter()
        TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples())
        val str = writer.getBuffer.toString

        ZIO.succeed(Response.text(str))
    }
  }

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val quillLayer: ZLayer[DataSource, Nothing, Quill.Postgres[CompositeNamingStrategy2[SnakeCase, PostgresEscape]]] =
      Quill.Postgres.fromNamingStrategy(CompositeNamingStrategy2(SnakeCase, PostgresEscape))

    val dsLayer: ZLayer[Any, Throwable, DataSource] = Quill.DataSource.fromPrefix("db")

    val userRepositoryLayer = dsLayer >>> quillLayer >>> UserRepositoryImpl.layer

    val responseTimeMiddleware = new ResponseTimeMiddleware()

    val loggingMiddleware = new LoggingMiddleware()

    val middlewares = loggingMiddleware ++ responseTimeMiddleware

    for {
      userRepositoryEnv <- userRepositoryLayer.build
      httpApp <- app.provideEnvironment(userRepositoryEnv)
      _ <- Server.serve(httpApp @@ middlewares).provide(Server.default)
    } yield ()
  }
}
