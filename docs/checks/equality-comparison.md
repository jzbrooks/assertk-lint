# EqualityComparisonAssertion

**Issue ID** `EqualityComparisonAssertion` ·
**Severity** `Warning` ·
**Category** `Usability` ·
**Default** on

Flags `assertThat(a == b)` (and `a != b`) where assertk's `isEqualTo` /
`isNotEqualTo` should be used.

## Why

Wrapping the comparison in `assertThat` and then asserting on the resulting
`Boolean` produces a useless failure message ("expected `true`, got `false`").
`isEqualTo` shows both sides of the comparison and uses assertk's diffing for
collections and data classes (see also `isDataClassEqualTo`).

## Example

=== "Flagged"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isTrue

    fun namesMatch() {
        val a = "Ada"
        val b = "Ada"
        assertThat(a == b).isTrue()
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isEqualTo

    fun namesMatch() {
        val a = "Ada"
        val b = "Ada"
        assertThat(a).isEqualTo(b)
    }
    ```

## Quick fix

Yes &mdash; the IDE picks the non-literal side as the subject, the
literal/string-template side as the expected value, and rewrites
`isTrue()`/`isFalse()` to `isEqualTo`/`isNotEqualTo`.

| Pattern | Becomes |
| --- | --- |
| `assertThat(a == b).isTrue()` | `assertThat(a).isEqualTo(b)` |
| `assertThat(a == b).isFalse()` | `assertThat(a).isNotEqualTo(b)` |
| `assertThat(a != b).isTrue()` | `assertThat(a).isNotEqualTo(b)` |
| `assertThat(a != b).isFalse()` | `assertThat(a).isEqualTo(b)` |

## Source

[`BooleanExpressionSubjectDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/BooleanExpressionSubjectDetector.kt)
