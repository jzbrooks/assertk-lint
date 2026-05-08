# KotlinAssertionUse

**Issue ID** `KotlinAssertionUse` ·
**Severity** `Warning` ·
**Category** `Correctness` ·
**Default** on

Flags calls to Kotlin's built-in `assert(condition)` (from
`kotlin.PreconditionsKt`) inside test sources.

## Why

`kotlin.assert` only runs when the JVM is started with `-ea` (assertions
enabled). In a typical test run that flag isn't set, so the call quietly
becomes a no-op and the test cannot fail. Even when assertions are enabled,
the failure message is just `"Assertion failed"` &mdash; assertk produces
descriptive, value-aware messages.

## Example

=== "Flagged"

    ```kotlin
    class TestingTesting {
        fun testingTest() {
            val first = File()
            assert(first.canRead) // silently no-ops without -ea
        }
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isTrue

    class TestingTesting {
        fun testingTest() {
            val first = File()
            assertThat(first.canRead).isTrue()
        }
    }
    ```

## Quick fix

Yes &mdash; the IDE rewrites `assert(x)` to `assertThat(x).isTrue()` and adds
the necessary imports (`assertk.assertThat`, `assertk.assertions.isTrue`).

## Source

[`KotlinAssertionDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/KotlinAssertionDetector.kt)
