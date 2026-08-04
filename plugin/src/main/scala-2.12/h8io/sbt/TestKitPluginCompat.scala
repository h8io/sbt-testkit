package h8io.sbt

/** sbt 1 caches nothing, so there is nothing to opt out of and `Def.uncached` does not exist. */
object TestKitPluginCompat {
  def uncached[A](value: A): A = value
}
