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

  override def projectSettings: Seq[Def.Setting[?]] =
    inConfig(TestKit)(Defaults.configSettings) ++
      addArtifact(TestKit / packageBin / artifact, TestKit / packageBin) ++
      addArtifact(TestKit / packageSrc / artifact, TestKit / packageSrc) ++
      addArtifact(TestKit / packageDoc / artifact, TestKit / packageDoc) ++
      Seq(
        Test / dependencyClasspath ++= (TestKit / exportedProducts).value,
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
