# BooleanEqualityAssertion

**Issue ID** `BooleanEqualityAssertion` ·
**Severity** `Warning` ·
**Category** `Usability` ·
**Default** on

Flags `assertThat(b).isEqualTo(true)` and `assertThat(b).isEqualTo(false)`
where assertk's `isTrue()` / `isFalse()` should be used.

## Why

`isTrue()` and `isFalse()` say what the test means directly. For a nullable
`Boolean?` subject, `isNotNull()` narrows the subject to `Boolean` first, so a
`null` fails with a clear message instead of an equality mismatch.

Subjects that are null or equality comparisons (`assertThat(x == null)`,
`assertThat(a == b)`) are left to [`NullComparisonAssertion`](null-comparison.md)
and [`EqualityComparisonAssertion`](equality-comparison.md). Those fixes treat
`isEqualTo(true)` / `isEqualTo(false)` like `isTrue()` / `isFalse()`, so
`assertThat(name == null).isEqualTo(false)` becomes `assertThat(name).isNotNull()`
in one step.

## Example

=== "Flagged"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isEqualTo

    fun isEmpty() {
        val flag: Boolean = list.isEmpty()
        assertThat(flag).isEqualTo(true)
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isTrue

    fun isEmpty() {
        val flag: Boolean = list.isEmpty()
        assertThat(flag).isTrue()
    }
    ```

## Quick fix

Yes.

| Pattern | Becomes |
| --- | --- |
| `assertThat(b).isEqualTo(true)` | `assertThat(b).isTrue()` |
| `assertThat(b).isEqualTo(false)` | `assertThat(b).isFalse()` |
| `assertThat(nullableB).isEqualTo(true)` | `assertThat(nullableB).isNotNull().isTrue()` |
| `assertThat(nullableB).isEqualTo(false)` | `assertThat(nullableB).isNotNull().isFalse()` |

## Source

[`BooleanExpressionSubjectDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/BooleanExpressionSubjectDetector.kt)
