ThisBuild / organization := "io.h8.sbt"
ThisBuild / organizationName := "H8IO"
ThisBuild / organizationHomepage := Some(url("https://github.com/h8io/"))

ThisBuild / description := "SBT testkit configuration plugin"
ThisBuild / licenses := List("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/h8io/sbt-testkit"))
ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / dynverSonatypeSnapshots := true
ThisBuild / dynverSeparator := "-"

ThisBuild / scalaVersion := "2.12.21"
ThisBuild / crossScalaVersions := Seq(scalaVersion.value, "3.8.4")
ThisBuild / javacOptions ++= Seq("--release", "11")

ThisBuild / developers := List(
  Developer(
    id = "eshu",
    name = "Pavel",
    email = "tjano.xibalba@gmail.com",
    url = url("https://github.com/h8io/")
  )
)

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/h8io/sbt-testkit"),
    "scm:git@github.com:h8io/sbt-testkit.git"
  )
)

val plugin = project
  .enablePlugins(SbtPlugin, ScoverageSummaryPlugin)
  .settings(
    name := "sbt-testkit",
    sbtPlugin := true,
    sbtPluginPublishLegacyMavenStyle := false,
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.8.0"
        case _ => "2.0.0"
      }
    },
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 12)) =>
        Seq(
          "-Xsource:3",
          "-language:higherKinds",
          "--deprecation",
          "--feature",
          "--unchecked",
          "-Xlint:_",
          "-Xfatal-warnings",
          "-opt:l:inline",
          "-opt-warnings",
          "-Ywarn-unused",
          "-Ywarn-dead-code",
          "-Ywarn-unused:-nowarn",
          "-Ypartial-unification"
        )
      case _ =>
        Seq(
          "-deprecation",
          "-feature",
          "-unchecked",
          "-Werror",
          "-Wshadow:all",
          "-Wunused:all",
          "-Wvalue-discard"
        )
    })
  )
