# UnusedAssertkAssertion

**Issue ID** `UnusedAssertkAssertion` ·
**Severity** `Error` ·
**Category** `Correctness` ·
**Default** on

Flags an `assertk.assertThat(...)` call whose result is never followed by an
assertion. Without an assertion the test cannot fail &mdash; the subject is
created and immediately discarded.

## Why

`assertThat` returns an `Assert<T>`. The actual check (`isEqualTo`, `isNotNull`,
…) is a method on that subject. If nothing is invoked on the subject, no
assertion runs, and the test silently passes regardless of the value.

The detector uses Lint's `TargetMethodDataFlowAnalyzer` to chase the subject
through the enclosing method/lambda. It also has a deliberate carve-out for
scope functions: `assertThat(x).apply { isEqualTo(y) }` and friends are
considered used as long as the lambda body contains at least one call on an
`assertk.Assert` receiver.

## Example

=== "Flagged"

    ```kotlin
    import assertk.assertThat

    class TestingTesting {
        fun testingTest() {
            val first = File()
            assertThat(first) // (1)
        }
    }
    ```

    1. No assertion is ever called on the subject &mdash; this test cannot fail.

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isEqualTo

    class TestingTesting {
        fun testingTest() {
            val first = File()
            val second = File()

            assertThat(first).isEqualTo(second)
        }
    }
    ```

It also catches the lambda-arg case:

```kotlin
enum class Scenario(assertion: () -> Unit) {
    LambdaScenario(assertion = { assertThat("") }) // flagged
}
```

## Quick fix

Available for several common cases &mdash; the detector recognises null
comparisons, equality comparisons, and `is`-type checks passed as the
`assertThat` argument and rewrites them into the proper assertion. For example:

```kotlin
assertThat(name == null)        // becomes
assertThat(name).isNull()
```

```kotlin
assertThat(value is String)     // becomes
assertThat(value).isInstanceOf<String>()
```

For other shapes, fix manually by chaining the appropriate assertion.

## Source

[`UnusedAssertionDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/UnusedAssertionDetector.kt)
