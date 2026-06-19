package h8io.sbt

import sbt.*
import sbt.Keys.*

object TestKitPluginCompat {
  def classpathSettings(config: Configuration): Seq[Def.Setting[?]] =
    Seq(
      Test / dependencyClasspath := Def.uncached(
        (Test / dependencyClasspath).value ++ (config / exportedProducts).value
      )
    )
}
