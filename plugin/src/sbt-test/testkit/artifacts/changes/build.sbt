val classifiers = taskKey[Set[String]]("Classifiers of the artifacts publishing would upload")

val expectTestKitArtifacts = taskKey[Unit]("Fail unless the three testkit artifacts are among them")

val expectNoTestKitArtifacts = taskKey[Unit]("Fail unless the three testkit artifacts are absent")

val testKitClassifiers = Set("testkit", "testkit-sources", "testkit-javadoc")

val root = (project in file("."))
  .enablePlugins(TestKitPlugin)
  .settings(
    name := "artifacts",
    organization := "io.h8.test",
    scalaVersion := "2.13.18",
    TestKit / publishTestKitArtifacts := false,
    // publishLocalConfiguration rather than packagedArtifacts: what matters is what publishing would upload
    classifiers := publishLocalConfiguration.value.artifacts.map(_._1).flatMap(_.classifier).toSet,
    expectTestKitArtifacts := {
      val actual = classifiers.value
      assert(testKitClassifiers.subsetOf(actual), s"expected $testKitClassifiers among $actual")
    },
    expectNoTestKitArtifacts := {
      val actual = classifiers.value
      assert(testKitClassifiers.intersect(actual).isEmpty, s"expected none of $testKitClassifiers among $actual")
    }
  )
