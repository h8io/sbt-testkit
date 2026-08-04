package app

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

// Seeing kit.Fixtures from Test is the whole point of the plugin, and the part TestKitPluginCompat implements
// differently for sbt 1 and sbt 2.
class GreetingTest extends AnyFlatSpec with Matchers {
  "Greeting" should "be visible to the testkit fixtures" in {
    Greeting(kit.Fixtures.name) shouldBe kit.Fixtures.expected
  }
}
