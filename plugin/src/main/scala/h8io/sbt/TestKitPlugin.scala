package h8io.sbt

import sbt.*
import sbt.Keys.*

object TestKitPlugin extends AutoPlugin {
  private val classifier: String = "testkit"

  override def requires: Plugins = plugins.JvmPlugin
  override def trigger: PluginTrigger = noTrigger

  object autoImport {
    val TestKit = config("testkit").extend(Compile).describedAs("TestKit configuration")
    val publishTestKitArtifacts = settingKey[Boolean]("Whether to publish TestKit artifacts.")
  }
  import autoImport.*

  override def projectConfigurations: Seq[Configuration] = Seq(TestKit)

  /** Registered through `Classpaths`, not through `addArtifact`: the latter appends unconditionally and never reads
    * `publishArtifact`, which is what left `publishTestKitArtifacts` unable to switch anything off. These two helpers
    * are what sbt uses for the artifacts of `Compile`, and they apply the `publishArtifact` of each task's own scope.
    */
  private val artifactTasks = Seq(TestKit / packageBin, TestKit / packageSrc, TestKit / packageDoc)

  override def projectSettings: Seq[Def.Setting[?]] =
    inConfig(TestKit)(Defaults.configSettings) ++
      Seq(
        Test / dependencyClasspath := TestKitPluginCompat.uncached(
          (Test / dependencyClasspath).value ++ (TestKit / exportedProducts).value),
        artifacts ++= Classpaths.artifactDefs(artifactTasks).value,
        packagedArtifacts := TestKitPluginCompat.uncached(
          packagedArtifacts.value ++ Classpaths.packaged(artifactTasks).value),
        TestKit / sourceDirectory := baseDirectory.value / "src" / classifier,
        TestKit / scalaSource := (TestKit / sourceDirectory).value / "scala",
        TestKit / resourceDirectory := (TestKit / sourceDirectory).value / "resources",
        TestKit / packageBin / artifact := (Compile / packageBin / artifact).value.withClassifier(Some(classifier)),
        TestKit / packageSrc / artifact :=
          (Compile / packageSrc / artifact).value.withClassifier(Some(classifier + "-sources")),
        TestKit / packageDoc / artifact :=
          (Compile / packageDoc / artifact).value.withClassifier(Some(classifier + "-javadoc")),
        TestKit / publishTestKitArtifacts := true,
        TestKit / packageBin / publishArtifact := (TestKit / publishTestKitArtifacts).value,
        TestKit / packageSrc / publishArtifact := (TestKit / publishTestKitArtifacts).value,
        TestKit / packageDoc / publishArtifact := (TestKit / publishTestKitArtifacts).value
      )
}
