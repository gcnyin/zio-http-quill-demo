lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging)
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
      "io.getquill" %% "quill-zio" % "4.6.1",
      "io.getquill" %% "quill-jdbc-zio" % "4.6.1",
      "org.postgresql" % "postgresql" % "42.5.4",
      "ch.qos.logback" % "logback-classic" % "1.4.8"
    )
  )