# Install

`assertk-lint` is published to Maven Central as `com.jzbrooks:assertk-lint`. It
plugs into the standard Android Lint pipeline through the `lintChecks`
configuration.

## Android module

```kotlin title="build.gradle.kts"
dependencies {
    lintChecks("com.jzbrooks:assertk-lint:1.5.1")
}
```

That's it. The next time `./gradlew lint` runs (or the IDE inspects the file)
the assertk checks will be active in test sources.

## JVM module

The Android Lint plugin can be applied to a plain JVM module too. You need the
`com.android.lint` Gradle plugin in addition to the dependency:

```kotlin title="build.gradle.kts"
plugins {
    id("com.android.lint") version "8.7.2"
}

dependencies {
    lintChecks("com.jzbrooks:assertk-lint:1.5.1")
}
```

## Version catalog

If you keep dependencies in a version catalog:

```toml title="gradle/libs.versions.toml"
[versions]
assertk-lint = "1.5.1"

[libraries]
assertk-lint = { module = "com.jzbrooks:assertk-lint", version.ref = "assertk-lint" }
```

```kotlin title="build.gradle.kts"
dependencies {
    lintChecks(libs.assertk.lint)
}
```

## Scope

All detectors run only against **test sources** &mdash; files in source sets
that the Android Gradle plugin marks as `isTestSource`. Production code is
never flagged, even if it imports assertk.

The detectors also short-circuit on Java sources. Every check requires
`KotlinLanguage`; mixed Java/Kotlin test trees are safe.

!!! tip "Verify the install"
    Add a deliberately unused `assertThat(...)` to a test file. Running
    `./gradlew lint` (or the IDE inspector) should flag
    [`UnusedAssertkAssertion`](checks/unused-assertion.md) as an **error**.
