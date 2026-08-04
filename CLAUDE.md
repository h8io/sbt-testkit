# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`sbt-testkit` is an sbt plugin that adds a `testkit` configuration to a project: sources in `src/testkit`, a
classpath entry for `Test`, and three published artifacts under the `testkit`, `testkit-sources` and
`testkit-javadoc` classifiers. It exists so that test utilities can be shared between modules and consumed by
downstream builds without leaking into the main artifact.

`h8io/stages` is the reference consumer — `core/src/testkit/scala` holds its shared test utilities, and other
modules reach them through `core % "test->testkit"`. Changing what the plugin publishes or how the
configuration is wired changes that build.

## Commands

```bash
sbt +test                         # unit tests on both rows of the matrix
sbt "plugin2_12/test"             # one row: sbt 1 on Scala 2.12
sbt "plugin/test"                 # the other: sbt 2 on Scala 3
sbt "coverageOff; +scripted"      # the scripted tests, both rows
sbt scalafmtAll scalafmtSbt       # format
./test.sh                         # everything CI runs
```

## The matrix

`projectMatrix` builds two rows: Scala 2.12 published for sbt 1, Scala 3 published for sbt 2. The row is what
decides which sbt API the code compiles against, so almost everything surprising here follows from it.

- **`baseDirectory` is virtual** (`.sbt/matrix/plugin2_12`), while `sourceDirectory` still points at `plugin/src`.
  Anything deriving paths from `baseDirectory` needs checking against both.
- **`scriptedSbt` is deliberately decoupled from `pluginCrossBuild / sbtVersion`.** The plugin is published for
  sbt 1.0 binary compatibility and so runs on any sbt 1.x, but it cannot be *tested* on the oldest: sbt 1.8.0
  brings Scala 2.12.17, which cannot read Java 21 class files and dies with `bad constant pool index` before the
  test build is loaded. Scripted therefore runs on a current sbt 1.x. The published artifact is unaffected.

## The compat split

`TestKitPluginCompat` exists twice, in `src/main/scala-2.12` and `src/main/scala-3`, under the same fully
qualified name; sbt adds the version directory for the row being compiled, so each row sees exactly one. The
shared code in `src/main/scala` calls it without knowing which.

It is down to a single method, and should stay that way:

```scala
def uncached[A](value: A): A = value                                  // 2.12
inline def uncached[A](inline value: A): A = Def.uncached(value)      // 3
```

**The cause is sbt 2, not Scala 3.** sbt 2 caches the result of every task unless told otherwise, and refuses to
compile one whose result type has no `sjsonnew.JsonFormat`. A classpath and a `Map[Artifact, …]` point at build
outputs, which is not something worth restoring from a previous run. `Def.uncached` does not exist in sbt 1 at
all, so the split cannot be a runtime branch — and the types differ too (`packageBin` yields a `File` on sbt 1
and an `xsbti.HashedVirtualFileRef` on sbt 2), which rules out reflection as well.

Two consequences worth keeping in mind:

- **Settings belong in the shared file**, not in the compat files. Duplicating a setting across two rows that
  are compiled separately is an invitation to fix one copy and forget the other. Where a type is all that
  differs, leaving it to inference is usually enough: `artifactTasks` needs no annotation and resolves to the
  right type on each row.
- **The directories are named on the Scala axis while the reason is the sbt axis.** They coincide today
  (2.12 ↔ sbt 1, 3 ↔ sbt 2). sbt also offers `scala-sbt-1.0` and `scala-sbt-2`, which would name the reason
  honestly; moving is a rename of two directories and no code.

## Testing

**Unit tests pin the arrangement of settings** — which keys the plugin defines, in which scopes, in which order.
The values behind them live inside setting macros and only exist once sbt evaluates them against a loaded build,
so they are unreachable from a unit test. Order matters and is asserted: sbt takes the last setting for a key,
so the plugin's overrides have to come after the `Defaults.configSettings` that also define them.

**Scripted tests pin the behaviour**, in `plugin/src/sbt-test/testkit/`. They generate real builds and run them
in both rows, which is the only place the two `uncached` implementations are actually exercised.

- `classpath` — a fixture in `src/testkit` reaching into `Compile`, and a test reaching into the fixture.
- `artifacts` — the three classifiers on `publishLocalConfiguration`, that is on what publishing would upload
  rather than on what packaging happens to produce, and their absence once `publishTestKitArtifacts` is off.

Write `Testkit / compile`, not `testkit:compile`: sbt 2 dropped the colon syntax and takes the configuration's
**id** where sbt 1 took its **name**. The capitalised form works in both.

`test.sh` runs scripted in a second sbt invocation with coverage explicitly off. The sbt server outlives one
invocation, so `+coverage` would otherwise carry into the artifact scripted publishes and then tests against.

## Coverage

The gate is `coverageSummaryStmt/BranchLowThreshold := 90`, high at 95, and the build measures 100%. Treat that
number with suspicion: it measures settings being *constructed*, since that is all a unit test can do here.
Nothing about it says the settings behave, which is why scripted carries the weight.

## Artifacts

Registration goes through `Classpaths.artifactDefs` and `Classpaths.packaged`, never `addArtifact`. The latter
appends unconditionally and never reads `publishArtifact`, which is exactly why `publishTestKitArtifacts` did
nothing for as long as it was used.

## Style

- Warnings are fatal on both rows, including unused imports — an `import sbt.*` left behind after an edit fails
  the build rather than warning.
- `scalafmt` covers `.sbt` files too (`scalafmtSbtCheck`), and CI checks both.
