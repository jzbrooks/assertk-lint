# AssertThatEqualsUsage

**Issue ID** `AssertThatEqualsUsage` ·
**Severity** `Error` ·
**Category** `Correctness` ·
**Default** on

Flags `equals(...)` calls made on an `assertk.Assert` receiver. `equals`
returns a `Boolean` &mdash; it does **not** perform an assertion, so the test
will silently pass regardless of the result.

## Why

`assertk.Assert` inherits `Any.equals(Any?): Boolean`. Calling it looks like an
assertion (`assertThat(a).equals(b)`) but actually just compares the
`Assert` wrapper to `b` and discards the result. The intended call is
`isEqualTo`.

The detector resolves the receiver type and only fires when the receiver
inherits from `assertk.Assert`, so unrelated `equals` calls in tests are not
flagged.

## Example

=== "Flagged"

    ```kotlin
    import assertk.assertThat

    fun test() {
        assertThat(1).equals(1) // never fails the test
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isEqualTo

    fun test() {
        assertThat(1).isEqualTo(1)
    }
    ```

## Quick fix

Yes &mdash; the IDE replaces `equals(x)` with `isEqualTo(x)` and adds the
`assertk.assertions.isEqualTo` import.

## Source

[`AssertThatEqualsDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/AssertThatEqualsDetector.kt)
