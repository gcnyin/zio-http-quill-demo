lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging, JavaAgent)
  .settings(
    name := "zio-http-quill-demo",
    organization := "com.github.gcnyin",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.11",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.15",
      "dev.zio" %% "zio-streams" % "2.0.15",
      "dev.zio" %% "zio-http" % "3.0.0-RC2",
      "dev.zio" %% "zio-prelude" % "1.0.0-RC19",
      "dev.zio" %% "zio-json" % "0.6.0",
      "dev.zio" %% "zio-logging" % "2.1.13",
      "dev.zio" %% "zio-logging-slf4j2" % "2.1.13",
      "io.getquill" %% "quill-zio" % "4.6.1",
      "io.getquill" %% "quill-jdbc-zio" % "4.6.1",
      "org.postgresql" % "postgresql" % "42.6.0",
      "p6spy" % "p6spy" % "3.9.1",
      "at.favre.lib" % "bcrypt" % "0.10.2",
      "io.prometheus" % "simpleclient_common" % "0.16.0",
      "org.slf4j" % "slf4j-api" % "2.0.7",
      "ch.qos.logback" % "logback-classic" % "1.4.8"
    ),
    javaAgents += "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % "1.28.0",
    dockerBaseImage := "eclipse-temurin:17.0.8_7-jre-jammy"
  )
