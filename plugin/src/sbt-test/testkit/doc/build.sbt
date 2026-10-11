val expectProject = inputKey[Unit]("Fail unless the scaladoc options of the scope name the given project")

def docProject(id: String) =
  Project(id, file(id))
    .enablePlugins(TestKitPlugin)
    .settings(
      name := s"doc-project-$id",
      scalaVersion := "3.9.0",
      // Each command evaluates one scope, so the order in which they run is the order in which sbt meets them
      Seq(Compile, TestKit).map { config =>
        config / expectProject := {
          val expected = complete.Parsers.spaceDelimited("<project>").parsed.mkString
          val options = (config / doc / scalacOptions).value
          assert(options.containsSlice(Seq("-project", expected)), s"expected -project $expected in $options")
        }
      }
    )

val a = docProject("a")
val b = docProject("b")
