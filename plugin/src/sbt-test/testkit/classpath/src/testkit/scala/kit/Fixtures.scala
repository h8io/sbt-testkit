package kit

// Lives in src/testkit, which only exists because the plugin moves the configuration's source directory there.
// It reaches into Compile, which only works because the TestKit configuration extends it.
object Fixtures {
  val name: String = "world"
  val expected: String = app.Greeting(name)
}
