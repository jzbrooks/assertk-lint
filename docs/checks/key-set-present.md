# KeySetPresentAssertion

**Issue ID** `KeySetPresentAssertion` ·
**Severity** `Warning` ·
**Category** `Usability` ·
**Default** on

Flags `assertThat(map.keys).contains(k)` &mdash; checking key presence by
materialising the key set. Use `Assert<Map<K, V>>.key(k)` directly.

## Why

`assertThat(map.keys).contains(k)` produces a generic "expected to contain X"
failure on a `Set<K>`. `key(k)` is purpose-built for the same intent and gives
a message that names the map. It also transforms the subject to the value
type, so you can chain further assertions if you want them.

## Example

=== "Flagged"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.contains

    fun keyExists() {
        val map = mapOf("9A3E6FAC" to "John")
        assertThat(map.keys).contains("9A3E6FAC")
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.key

    fun keyExists() {
        val map = mapOf("9A3E6FAC" to "John")
        assertThat(map).key("9A3E6FAC")
    }
    ```

## Quick fix

Yes &mdash; the IDE rewrites the entire `assertThat(map.keys).contains(k)`
expression to `assertThat(map).key(k)` and adds `assertk.assertions.key`.

## Source

[`MapAssertionDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/MapAssertionDetector.kt)
