package h8io.sbt

import sbt.*

/** sbt 2 caches the result of every task unless told otherwise, and refuses to compile a task whose result type has no
  * `JsonFormat`. A classpath and a map of packaged artifacts point at build outputs, which is not something worth
  * restoring from a previous run, so they opt out.
  */
object TestKitPluginCompat {
  inline def uncached[A](inline value: A): A = Def.uncached(value)
}
