package h8io.sbt

import h8io.sbt.TestKitPlugin.autoImport.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sbt.*
import sbt.Keys.*
import sbt.plugins.JvmPlugin

/** What this plugin does is arrange settings, so what there is to pin down is the arrangement: which keys it defines,
  * in which scopes, and in which order. The values behind the keys live inside setting macros and only exist once sbt
  * evaluates them against a loaded build, so they are out of reach from here.
  */
class TestKitPluginTest extends AnyFlatSpec with Matchers {
  private val settings = TestKitPlugin.projectSettings

  private def labels(ss: Seq[Def.Setting[?]]) = ss.map(_.key.key.label).toSet

  private def inConfig(config: Configuration)(ss: Seq[Def.Setting[?]]) =
    ss.filter(_.key.scope.config == Select(ConfigKey(config.name)))

  private def forTask(key: Scoped)(ss: Seq[Def.Setting[?]]) = ss.filter(_.key.scope.task == Select(key.key))

  private def withLabel(key: Scoped)(ss: Seq[Def.Setting[?]]) = ss.filter(_.key.key.label == key.key.label)

  private val packageTasks = Seq(Select(packageBin.key), Select(packageSrc.key), Select(packageDoc.key))

  "TestKitPlugin" should "be enabled explicitly rather than triggered" in {
    // A configuration and three extra published artifacts are not something a project should acquire by accident
    TestKitPlugin.trigger shouldEqual PluginTrigger.NoTrigger
    TestKitPlugin.requires shouldEqual JvmPlugin
  }

  it should "add the TestKit configuration to the project" in {
    // Without this the configuration would exist as a value but no project would carry it, and a dependency of the
    // form `core % "test->testkit"` would have nothing to point at
    TestKitPlugin.projectConfigurations shouldEqual Seq(TestKit)
  }

  "the TestKit configuration" should "be named after the classifier it publishes under" in {
    // The artifact classifiers are derived from this name, so renaming the configuration renames the artifacts
    TestKit.name shouldEqual "testkit"
  }

  it should "extend Compile" in {
    // Otherwise the testkit sources would not see the main ones
    TestKit.extendsConfigs.map(_.name) should contain(Compile.name)
  }

  "the configuration" should "carry the standard compile settings" in {
    // inConfig(TestKit)(Defaults.configSettings) is what makes `testkit:compile` exist at all
    labels(inConfig(TestKit)(settings)) should contain allOf (Keys.compile.key.label, products.key.label)
  }

  it should "take its sources from src/testkit rather than from the default of the configuration" in {
    labels(inConfig(TestKit)(settings)) should contain allOf (
      sourceDirectory.key.label,
      scalaSource.key.label,
      resourceDirectory.key.label
    )
  }

  it should "define those after the defaults, which set them too" in {
    // sbt takes the last setting for a key, so an override placed before the defaults would simply be discarded
    val defaults = settings.lastIndexWhere(_.key.key.label == Keys.compile.key.label)
    settings.lastIndexWhere(_.key.key.label == sourceDirectory.key.label) should be > defaults
  }

  "the packaging" should "give each of the three package tasks an artifact of its own" in {
    val artifactSettings = withLabel(artifact)(settings)
    artifactSettings.map(_.key.scope.task).distinct should contain theSameElementsAs packageTasks
    artifactSettings.map(_.key.scope.config).distinct shouldEqual Seq(Select(ConfigKey(TestKit.name)))
  }

  it should "define the classifiers after the defaults, which define artifacts of their own" in {
    val defaults = settings.lastIndexWhere(_.key.key.label == Keys.compile.key.label)
    forTask(packageBin)(withLabel(artifact)(settings)) should have size 2
    settings.lastIndexWhere(s => s.key.key.label == artifact.key.label) should be > defaults
  }

  it should "register them for publishing outside the configuration" in {
    // Scoped into TestKit these would describe artifacts nobody publishes, since publishing reads them from the
    // project scope
    val registrations = withLabel(artifacts)(settings) ++ withLabel(packagedArtifacts)(settings)
    registrations should have size 2
    registrations.map(_.key.scope.config).distinct shouldEqual Seq(This)
  }

  it should "gate each of the three on publishTestKitArtifacts" in {
    forTask(packageBin)(withLabel(publishArtifact)(inConfig(TestKit)(settings))) should have size 1
    withLabel(publishArtifact)(inConfig(TestKit)(settings)).map(_.key.scope.task) should
      contain theSameElementsAs packageTasks
  }

  it should "offer that switch as a setting of the configuration" in {
    labels(inConfig(TestKit)(settings)) should contain(publishTestKitArtifacts.key.label)
  }

  /** The one place with two implementations — one for sbt 1 on 2.12, one for sbt 2 on 3. They differ in how they extend
    * the classpath, and this is what they are not allowed to differ on. These run in both rows of the matrix, so a
    * change to either file has to keep satisfying them.
    */
  "TestKitPluginCompat" should "contribute a single classpath setting" in {
    val compat = TestKitPluginCompat.classpathSettings(TestKit)
    compat should have size 1
    compat.head.key.key.label shouldEqual dependencyClasspath.key.label
  }

  it should "extend the classpath of Test, not the one of the configuration it is given" in {
    // The point of the plugin is that test code sees the testkit sources, not the other way round
    inConfig(Test)(TestKitPluginCompat.classpathSettings(TestKit)) should have size 1
    inConfig(TestKit)(TestKitPluginCompat.classpathSettings(TestKit)) shouldBe empty
  }

  it should "be part of what the plugin contributes" in {
    withLabel(dependencyClasspath)(inConfig(Test)(settings)) should have size 1
  }

  it should "register the artifacts through the project-scoped keys" in {
    // Not through addArtifact, which registers unconditionally and never reads publishArtifact — that is what left
    // publishTestKitArtifacts unable to switch anything off
    val artifactSettings = TestKitPluginCompat.artifactSettings(TestKit)
    artifactSettings.map(_.key.key.label) should contain theSameElementsAs
      Seq(artifacts.key.label, packagedArtifacts.key.label)
    artifactSettings.map(_.key.scope.config).distinct shouldEqual Seq(This)
  }
}
