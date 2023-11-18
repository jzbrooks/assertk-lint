# assertk-lint

[![build](https://github.com/jzbrooks/assertk-lint/actions/workflows/build.yml/badge.svg)](https://github.com/jzbrooks/assertk-lint/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.jzbrooks/assertk-lint.svg)](https://mvnrepository.com/artifact/com.jzbrooks/assertk-lint)

A set of lint rules to encourage proper use of assertk

### Installation

_Note: For non-android projects, you must apply the com.android.lint Gradle plugin._

```kotlin
dependencies {
  lintChecks("com.jzbrooks:assertk-lint:<version>")
}

// Your lint configuration must opt-in to running lint on test source!
lint {
    checkTestSources = true
}
```
