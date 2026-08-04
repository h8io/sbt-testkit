package h8io.sbt

import sbt.*
import sbt.Keys.*

object TestKitPluginCompat {
  private def artifactTasks(config: Configuration): Seq[TaskKey[File]] =
    Seq(config / packageBin, config / packageSrc, config / packageDoc)

  def classpathSettings(config: Configuration): Seq[Def.Setting[?]] =
    Seq(Test / dependencyClasspath ++= (config / exportedProducts).value)

  def artifactSettings(config: Configuration): Seq[Def.Setting[?]] =
    Seq(
      artifacts ++= Classpaths.artifactDefs(artifactTasks(config)).value,
      packagedArtifacts ++= Classpaths.packaged(artifactTasks(config)).value
    )
}
