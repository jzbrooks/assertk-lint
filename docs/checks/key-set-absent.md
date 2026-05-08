# KeySetAbsentAssertion

**Issue ID** `KeySetAbsentAssertion` ·
**Severity** `Warning` ·
**Category** `Usability` ·
**Default** on

Flags `assertThat(map.keys).doesNotContain(k)` &mdash; checking key absence by
materialising the key set. Use `Assert<Map<K, V>>.doesNotContainKey(k)`
directly.

## Why

The `Iterable.doesNotContain` form produces a generic message about a `Set<K>`.
`doesNotContainKey` is purpose-built for the same intent, names the map in the
failure, and reads more clearly at the call site.

## Example

=== "Flagged"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.doesNotContain

    fun keyMissing() {
        val map = mapOf("9A3E6FAC" to "John")
        assertThat(map.keys).doesNotContain("missing")
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.doesNotContainKey

    fun keyMissing() {
        val map = mapOf("9A3E6FAC" to "John")
        assertThat(map).doesNotContainKey("missing")
    }
    ```

## Quick fix

Yes &mdash; the IDE rewrites
`assertThat(map.keys).doesNotContain(k)` to `assertThat(map).doesNotContainKey(k)`
and adds `assertk.assertions.doesNotContainKey`.

## Source

[`MapAssertionDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/MapAssertionDetector.kt)
