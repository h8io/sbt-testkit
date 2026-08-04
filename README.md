[![GitHub release](https://img.shields.io/github/v/release/h8io/sbt-testkit)](https://github.com/h8io/sbt-testkit/releases/latest)

# sbt-testkit

Test support that ships **with** a library rather than **inside** it.

## The problem

A module accumulates helpers its tests are built on — generators, fixtures, matchers, base classes. Other modules
want them, and so, eventually, does anyone writing their own implementation of your abstractions. Neither of the
usual places works:

- **`src/main`** puts them in the published artifact, and drags ScalaTest and ScalaCheck onto the compile
  classpath of everyone who depends on you.
- **`src/test`** with `"test->test"` shares them inside your build and nowhere else, because test classes are not
  published.

That leaves a whole extra module, with its own name and coordinates, for what is really one module's other half.

## What the plugin adds

A third configuration next to `Compile` and `Test`:

- sources in **`src/testkit`**, separate from both
- it **extends `Compile`**, so the helpers can use the module they belong to
- its output is on the module's own **`Test`** classpath, so its tests use them directly
- it is **published** — under the same coordinates as the main artifact, with the classifiers `testkit`,
  `testkit-sources` and `testkit-javadoc`

## Installing

```scala
// project/plugins.sbt
addSbtPlugin("io.h8.sbt" % "sbt-testkit" % "2.1.1")
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

This is the part `"test->test"` cannot do, and the reason the plugin exists.

## Publishing

On by default. To keep the helpers inside the build and out of the release:

```scala
TestKit / publishTestKitArtifacts := false
```

## sbt versions

Published for both sbt 1.x and sbt 2.x; `addSbtPlugin` resolves the right one.

## License

Apache-2.0. See `LICENSE`.
