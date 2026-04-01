package h8io.sbt

import sbt.*
import sbt.Keys.*

object TestKitPlugin extends AutoPlugin {
  private def testkit: String = "testkit"

  override def requires: Plugins = plugins.JvmPlugin
  override def trigger: PluginTrigger = noTrigger

  object autoImport {
    val TestKit = config("testkit").extend(Compile).describedAs("TestKit configuration")
  }
  import autoImport.*
  val enabled: SettingKey[Boolean] = settingKey[Boolean]("Whether to publish the artifact.")

  override def projectConfigurations: Seq[Configuration] = Seq(TestKit)

  override def projectSettings: Seq[Def.Setting[?]] =
    inConfig(TestKit)(Defaults.configSettings) ++ Seq(
      TestKit / sourceDirectory := (ThisProject / baseDirectory).value / "src" / testkit,
      TestKit / scalaSource := (TestKit / sourceDirectory).value / "scala",
      TestKit / resourceDirectory := (TestKit / sourceDirectory).value / "resources",
      Test / unmanagedSourceDirectories ++= (TestKit / unmanagedSourceDirectories).value,
      Test / unmanagedResourceDirectories ++= (TestKit / unmanagedResourceDirectories).value,
      TestKit / enabled := true,
      TestKit / packageBin / artifact := (Compile / packageBin / artifact).value.withClassifier(Some(testkit)),
      TestKit / packageSrc / artifact :=
        (Compile / packageSrc / artifact).value.withClassifier(Some(testkit + "-sources")),
      TestKit / packageDoc / artifact :=
        (Compile / packageDoc / artifact).value.withClassifier(Some(testkit + "-javadoc")),
      TestKit / artifacts ++= {
        if ((TestKit / enabled).value)
          Seq(
            (TestKit / packageBin / artifact).value,
            (TestKit / packageSrc / artifact).value,
            (TestKit / packageDoc / artifact).value
          )
        else Nil
      },
      TestKit / packagedArtifacts ++= {
        if ((TestKit / enabled).value)
          Map(
            (TestKit / packageBin / artifact).value -> (TestKit / packageBin).value,
            (TestKit / packageSrc / artifact).value -> (TestKit / packageSrc).value,
            (TestKit / packageDoc / artifact).value -> (TestKit / packageDoc).value
          )
        else Map.empty[Artifact, File]
      }
    )
}
