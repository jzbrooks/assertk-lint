# assertk-lint

[![build](https://github.com/jzbrooks/assertk-lint/actions/workflows/build.yml/badge.svg)](https://github.com/jzbrooks/assertk-lint/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.jzbrooks/assertk-lint.svg)](https://mvnrepository.com/artifact/com.jzbrooks/assertk-lint)

A set of lint rules to encourage proper use of [assertk](https://github.com/willowtreeapps/assertk)

### Installation

_Note: For non-android projects, you must apply the com.android.lint Gradle plugin._

```kotlin
dependencies {
  lintChecks("com.jzbrooks:assertk-lint:<version>")
}
```
