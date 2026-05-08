# TryCatchAssertion

**Issue ID** `TryCatchAssertion` ·
**Severity** `Warning` ·
**Category** `Correctness` ·
**Default** on

Flags simple `try { ... } catch (e: Exception) { assertThat(e)... }` blocks
whose only purpose is to assert on an expected exception. Use
`assertk.assertFailure { ... }` instead.

## Why

A `try`/`catch` whose `catch` body only contains `assertThat(e)` calls is
fragile: if you forget to put a `fail()` after the throwing call (or the call
silently doesn't throw), the test passes when it shouldn't. `assertFailure`
inverts the contract &mdash; the block *must* throw, and you assert on the
captured `Throwable` directly.

The detector is intentionally conservative. It only fires when:

- the `try` block has exactly one non-`fail()` call, and
- the single `catch` clause's body contains *only* assertk
  `assertThat(...)` chains.

Anything else (multiple statements, mixed work, multiple catches) is left alone
because the block likely exists for reasons beyond exception assertion.

## Example

=== "Flagged"

    ```kotlin
    import assertk.fail
    import assertk.assertThat
    import assertk.assertions.isEqualTo
    import assertk.assertions.prop

    fun explodes() {
        val engine = RotatingDetonationEngine()
        try {
            engine.rotate()
            fail()
        } catch (e: Exception) {
            assertThat(e).prop(Exception::message).isEqualTo("Boom!")
        }
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertFailure
    import assertk.assertions.isEqualTo
    import assertk.assertions.messageContains
    import assertk.assertions.prop

    fun explodes() {
        val engine = RotatingDetonationEngine()
        assertFailure { engine.rotate() }
            .prop(Throwable::message)
            .isEqualTo("Boom!")
    }
    ```

## Quick fix

Not provided &mdash; the rewrite is structural (delete the `try`/`catch`,
extract the throwing call, hoist the catch-body assertions onto an
`assertFailure` chain) and depends on details that can't always be inferred
mechanically.

## Source

[`TryCatchDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/TryCatchDetector.kt)
