libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.20" % Test

val root = (project in file("."))
  .enablePlugins(TestKitPlugin)
  .settings(name := "classpath", organization := "io.h8.test", scalaVersion := "2.13.18")
