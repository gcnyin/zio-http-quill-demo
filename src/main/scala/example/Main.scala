package example

import at.favre.lib.crypto.bcrypt.BCrypt
import example.middleware.LoggingMiddleware
import example.repository.{UserRepository, UserRepositoryImpl}
import example.request.CreateUserRequest
import example.response.{CreateUserResponse, ListUserResponse}
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
      case Method.GET -> Root / "user" / "list-user" =>
        val response = for {
          users <- userRepository.listUser
            .logError("listUser error")
            .mapError { e => ErrorMsg.internalError(e.getMessage) }

          response = ListUserResponse(users = users.map { case (userId, username) =>
            ListUserResponse.User(userId = userId, username = username)
          })
        } yield Response.json(response.toJson)
        response.catchAll(errorMsg => ZIO.succeed(Response.json(errorMsg.toJson)))

      case req @ Method.POST -> Root / "user" / "create-user" =>
        val response = for {
          rawBody <- req.body.asString
            .logError("decode body string error")
            .mapError { e => ErrorMsg.invalidBody(e.getMessage) }
          request <- ZIO
            .fromEither(rawBody.fromJson[CreateUserRequest])
            .logError("CreateUserRequest decode error")
            .mapError { e => ErrorMsg.invalidRequest(e) }

          password <- ZIO.succeed(BCrypt.withDefaults().hashToString(8, request.password.toCharArray))

          userId <- userRepository
            .createUser(username = request.username, password = password)
            .mapError { e => new RuntimeException(e) }
            .logError("createUser error")
            .mapError { e => ErrorMsg.internalError(e.getMessage) }

          _ <- ZIO.log(s"created user: ${request.username}")
        } yield Response.json(CreateUserResponse(userId = userId).toJson)
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

    val loggingMiddleware = new LoggingMiddleware()

    val middlewares = loggingMiddleware

    for {
      userRepositoryEnv <- userRepositoryLayer.build
      httpApp <- app.provideEnvironment(userRepositoryEnv)
      _ <- Server.serve(httpApp @@ middlewares).provide(Server.default)
    } yield ()
  }
}
