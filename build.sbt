dynverSonatypeSnapshots := true
dynverSeparator := "-"

ThisBuild / coverageSummaryStmtLowThreshold := 90
ThisBuild / coverageSummaryStmtHighThreshold := 95
ThisBuild / coverageSummaryBranchLowThreshold := 90
ThisBuild / coverageSummaryBranchHighThreshold := 95

val plugin = projectMatrix.in(file("plugin"))
  .jvmPlatform(scalaVersions = Seq("3.9.0", "2.12.21"))
  .enablePlugins(SbtPlugin, ScoverageSummaryPlugin)
  .settings(
    name := "sbt-testkit",
    organization := "io.h8.sbt",
    organizationName := "H8IO",
    organizationHomepage := Some(url("https://github.com/h8io/")),
    description := "SBT testkit configuration plugin",
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage := Some(url("https://github.com/h8io/sbt-testkit")),
    versionScheme := Some("semver-spec"),
    javacOptions ++= Seq("--release", "11"),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.20" % Test,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", s"-Dplugin.version=${version.value}"),
    scriptedBufferLog := false,
    // The plugin is built for sbt 1.0 binary compatibility, so it runs on any sbt 1.x; scripted may therefore use a
    // current one instead of the oldest supported. It has to: sbt 1.8.0 brings Scala 2.12.17, which cannot read
    // Java 21 class files and dies with "bad constant pool index" before the test build is even loaded.
    scriptedSbt :=
      (scalaBinaryVersion.value match {
        case "2.12" => "1.12.14"
        case _ => (pluginCrossBuild / sbtVersion).value
      }),
    developers := List(
      Developer(
        id = "eshu",
        name = "Pavel",
        email = "tjano.xibalba@gmail.com",
        url = url("https://github.com/eshu/")
      )
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/h8io/sbt-testkit"),
        "scm:git@github.com:h8io/sbt-testkit.git"
      )
    ),
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.8.0"
        case _ => "2.0.0"
      }
    },
    scalacOptions ++=
      (scalaBinaryVersion.value match {
        case "2.12" =>
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
            "-Ywarn-value-discard",
            "-Ywarn-numeric-widen",
            "-Ywarn-extra-implicit",
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
            "-Wvalue-discard",
            "-Wsafe-init",
            "-source:future"
          )
      })
  )
