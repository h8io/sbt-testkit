[![GitHub release](https://img.shields.io/github/v/release/h8io/sbt-testkit)](https://github.com/h8io/sbt-testkit/releases/latest)

# sbt-testkit

Test support that ships **with** a library rather than **inside** it.

## The problem

A module accumulates helpers its tests are built on — generators, fixtures, matchers, base classes. They point
both ways at once: they are written against the module, and the module's own tests are written against them.
Other modules want them too, and so, eventually, does anyone implementing your abstractions in their own project.

Nothing ordinary holds that shape.

- **`src/main`** publishes the helpers inside the artifact and drags ScalaTest and ScalaCheck onto the compile
  classpath of everyone who depends on you.
- **`src/test`** with `"test->test"` gets both directions right and stops at the edge of your build: test classes
  are not published, so nobody outside can reuse them.
- **a separate module** can be published, but then it cannot serve the tests of the module it is built on. The two
  projects would have to depend on each other, and a build like that does not load at all — evaluating either
  definition requires the other, and sbt dies with a `StackOverflowError` before it reaches your code.

The helpers are not a second project. They are a third part of one.

## What the plugin adds

A third configuration next to `Compile` and `Test`, which is where code of that shape belongs:

- sources in **`src/testkit`**, separate from both
- it **extends `Compile`**, so the helpers are written against the module
- its output is on the module's own **`Test`** classpath, so the module's tests are written against the helpers
- it is **published** — under the same coordinates as the main artifact, with the classifiers `testkit`,
  `testkit-sources` and `testkit-javadoc`

The first two are the loop that no arrangement of projects can express. The third is what carries it past your
own build.

## Installing

```scala
// project/plugins.sbt
addSbtPlugin("io.h8.sbt" % "sbt-testkit" % "2.2.0")
```

```scala
// build.sbt
val core = (project in file("core"))
  .enablePlugins(TestKitPlugin)
  .settings(libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.20" % TestKit)
```

Test libraries belong in the `TestKit` configuration: the helpers are compiled against them, and anyone depending
on the published testkit artifact gets them transitively.

## Using it inside the build

`core`'s own tests need no wiring — the helpers are already on their classpath. Other modules ask:

```scala
val lib = (project in file("lib"))
  .dependsOn(core, core % "test->testkit")
```

`lib`'s tests now see `core`'s helpers, while `lib`'s main sources do not.

## Using it from another build

The helpers sit on Maven Central next to the library, so a downstream project asks for the classifier:

```scala
libraryDependencies += "io.h8" %% "stages-core" % "0.0.22" % Test classifier "testkit"
```

Same coordinates as the library, one classifier apart. This is the reach `"test->test"` does not have.

## Publishing

On by default. To keep the helpers inside the build and out of the release:

```scala
TestKit / publishTestKitArtifacts := false
```

## sbt versions

Published for both sbt 1.x and sbt 2.x; `addSbtPlugin` resolves the right one.

## License

Apache-2.0. See `LICENSE`.
