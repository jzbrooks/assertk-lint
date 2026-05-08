# NullComparisonAssertion

**Issue ID** `NullComparisonAssertion` ·
**Severity** `Warning` ·
**Category** `Usability` ·
**Default** on

Flags `assertThat(x == null)` (and `x != null`, plus the literal-on-the-left
forms) where assertk's purpose-built `isNull` / `isNotNull` should be used.

## Why

Comparing the value to `null` and asserting on the resulting `Boolean` works
but loses information &mdash; the failure message will say "expected `false`,
got `true`" instead of naming the actual value. assertk's `isNotNull` also
**transforms** the assertion subject from `Assert<T?>` to `Assert<T>`, letting
you chain further assertions on the non-null type without an explicit cast.

## Example

=== "Flagged"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isFalse

    fun nameExists() {
        val name: String? = this::class.simpleName
        assertThat(name == null).isFalse()
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isNotNull

    fun nameExists() {
        val name: String? = this::class.simpleName
        assertThat(name).isNotNull()
    }
    ```

## Quick fix

Yes &mdash; the IDE collapses the binary expression and rewrites the trailing
`isTrue()`/`isFalse()` into the appropriate `isNull()` or `isNotNull()`,
inserting the matching import.

| Pattern | Becomes |
| --- | --- |
| `assertThat(x == null).isTrue()` | `assertThat(x).isNull()` |
| `assertThat(x == null).isFalse()` | `assertThat(x).isNotNull()` |
| `assertThat(x != null).isTrue()` | `assertThat(x).isNotNull()` |
| `assertThat(x != null).isFalse()` | `assertThat(x).isNull()` |

## Source

[`BooleanExpressionSubjectDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/BooleanExpressionSubjectDetector.kt)
