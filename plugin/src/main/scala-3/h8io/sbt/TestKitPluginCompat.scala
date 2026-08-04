package h8io.sbt

import sbt.*
import sbt.Keys.*

object TestKitPluginCompat {
  private def artifactTasks(config: Configuration): Seq[TaskKey[xsbti.HashedVirtualFileRef]] =
    Seq(config / packageBin, config / packageSrc, config / packageDoc)

  def classpathSettings(config: Configuration): Seq[Def.Setting[?]] =
    Seq(
      Test / dependencyClasspath := Def.uncached(
        (Test / dependencyClasspath).value ++ (config / exportedProducts).value
      )
    )

  def artifactSettings(config: Configuration): Seq[Def.Setting[?]] =
    Seq(
      artifacts ++= Classpaths.artifactDefs(artifactTasks(config)).value,
      // The result carries no JsonFormat, so sbt 2 refuses to cache it
      packagedArtifacts := Def.uncached(packagedArtifacts.value ++ Classpaths.packaged(artifactTasks(config)).value)
    )
}
