import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}
import higherkindness.mu.rpc.srcgen.Model.IdlType

val Http4sVersion          = "0.21.16"
val CirceVersion           = "0.13.0"
val CirceConfigVersion     = "0.8.0"
val MunitVersion           = "0.7.20"
val LogbackVersion         = "1.2.3"
val MunitCatsEffectVersion = "0.13.0"
val HigherKindnessV        = "0.25.0"
val SubsVersion            = "20.2.0"
val DoobieVersion          = "0.13.0"
val FlywayVersion          = "5.2.4"
val MockitoVersion         = "1.16.3"
val PgEmbeddedVersion      = "1.2.10"

val postgresV = "10.11.0"
val osVersion =
  System.getProperty("os.name").toLowerCase match {
    case osName if osName.contains("mac") =>
      "embedded-postgres-binaries-darwin-amd64"
    case osName if osName.contains("win") =>
      "embedded-postgres-binaries-windows-amd64"
    case osName if osName.contains("linux") =>
      "embedded-postgres-binaries-linux-amd64"
    case osName => throw new RuntimeException(s"Unknown operating system $osName")
  }

val Dependencies = Seq(
  "org.http4s"        %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"        %% "http4s-blaze-client" % Http4sVersion,
  "org.http4s"        %% "http4s-circe"        % Http4sVersion,
  "org.http4s"        %% "http4s-dsl"          % Http4sVersion,
  "io.circe"          %% "circe-generic"       % CirceVersion,
  "io.circe"          %% "circe-config"        % CirceConfigVersion,
  "io.higherkindness" %% "mu-rpc-client-netty" % HigherKindnessV,
  "ch.qos.logback"     % "logback-classic"     % LogbackVersion,
  "org.scalameta"     %% "svm-subs"            % SubsVersion,
  "org.tpolecat"      %% "doobie-core"         % DoobieVersion,
  "org.tpolecat"      %% "doobie-hikari"       % DoobieVersion,
  "org.tpolecat"      %% "doobie-postgres"     % DoobieVersion,
  "org.flywaydb"       % "flyway-core"         % FlywayVersion,
  "io.zonky.test"      % "embedded-postgres"   % PgEmbeddedVersion      % "it,test",
  "org.mockito"       %% "mockito-scala"       % MockitoVersion         % "it,test",
  "org.scalameta"     %% "munit"               % MunitVersion           % "it,test",
  "org.typelevel"     %% "munit-cats-effect-2" % MunitCatsEffectVersion % "it,test",
  // Needed to build an in-memory server in the test
  "io.higherkindness" %% "mu-rpc-testing" % "0.25.0" % "it,test"
)

lazy val macroSettings: Seq[Setting[_]] = Seq(
  libraryDependencies ++= Seq(
    scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided
  ),
  scalacOptions ++= Seq("-Ymacro-annotations", "-language:higherKinds")
)

lazy val root = (project in file("."))
  .enablePlugins(SrcGenPlugin)
  .enablePlugins(JavaAppPackaging, BuildInfoPlugin)
  .enablePlugins(DockerPlugin, DockerComposePlugin)
  .configs(IntegrationTest)
  .settings(
    // it settings
    Defaults.itSettings,
    inConfig(IntegrationTest.extend(Test))(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings)
  )
  .settings(
    packageName in Docker := packageName.value,
    version in Docker := version.value,
    maintainer in Docker := organization.value,
    dockerRepository := Some("tools.radicalbit.io"),
    defaultLinuxInstallLocation in Docker := s"/opt/${packageName.value}",
    dockerCommands := Seq(
      Cmd("FROM", "adoptopenjdk/openjdk11:alpine"),
      Cmd(
        "RUN",
        "apk",
        "update",
        "&&",
        "apk",
        "add",
        "--no-cache",
        "--update",
        "tzdata",
        "openssl",
        "bash",
        "curl",
        "eudev",
        """'su-exec>=0.2'"""
      ),
      Cmd("WORKDIR", s"/opt/${packageName.value}"),
      Cmd("RUN", "addgroup", "-S", "spixi", "&&", "adduser", "-S", "spixi", "-G", "spixi"),
      Cmd("ADD", "opt", "/opt"),
      ExecCmd("RUN", "chown", "-R", "spixi:spixi", "."),
      Cmd("USER", "spixi"),
      Cmd("CMD", s"bin/${packageName.value}")
    ),
    dockerImageCreationTask := (publishLocal in Docker).value,
    variablesForSubstitution := Map(
      "image_name"      -> s"${dockerRepository.value.get}/${packageName.value}",
      "project_version" -> version.value
    )
  )
  .settings(
    libraryDependencies ++= Seq(
      // Needed for the generated code to compile
      "io.higherkindness" %% "mu-rpc-service" % "0.25.0"
    ),
    macroSettings,
    // Generate sources from .proto files
    muSrcGenIdlType := IdlType.Proto,
    // Make it easy for 3rd-party clients to communicate with us via gRPC
    muSrcGenIdiomaticEndpoints := true
  )
  .settings(
    organization := "org.spixi",
    name := "http4s-playground",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.4",
    scalafmtOnCompile := true,
    libraryDependencies ++= Dependencies,
    addCompilerPlugin("org.typelevel" % "kind-projector"     % "0.11.3" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
