package h8io.sbt

import sbt.*
import sbt.Keys.*

object TestKitPluginCompat {
  def classpathSettings(config: Configuration): Seq[Def.Setting[?]] =
    Seq(Test / dependencyClasspath ++= (config / exportedProducts).value)
}
