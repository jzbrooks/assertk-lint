# MapValueAssertion

**Issue ID** `MapValueAssertion` ·
**Severity** `Warning` ·
**Category** `Correctness` ·
**Default** on

Flags `assertThat(map[key])`, `assertThat(map.get(key))`,
`assertThat(map.getOrElse(...))`, and `assertThat(map.getValue(key))` &mdash;
all of which read a value out of the map *before* the assertion subject is
created. Use `Assert<Map<K, V>>.key(key): Assert<V>` instead.

## Why

Pulling the value out of the map first hides the failure mode: if the key is
absent, `Map.get` returns `null` (and you'd be asserting on `null`), and
`Map.getValue` throws a `NoSuchElementException` (which looks like a crash
rather than a test failure). `key(k)` asserts that the key is present *and*
transforms the subject to `Assert<V>` so you can chain further assertions on
the value.

Recognised map accessors: `get`, `getValue`, `getOrElse`, `getOrDefault`, plus
the `[]` operator.

## Example

=== "Flagged"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isNotNull

    fun lookup() {
        val map: Map<String, String?> = mapOf("9A3E6FAC" to "John")
        assertThat(map["9A3E6FAC"]).isNotNull()
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.key
    import assertk.assertions.isNotNull

    fun lookup() {
        val map: Map<String, String?> = mapOf("9A3E6FAC" to "John")
        assertThat(map).key("9A3E6FAC").isNotNull()
    }
    ```

## Quick fix

Yes &mdash; the IDE rewrites `assertThat(map[k])` to `assertThat(map).key(k)`
and adds `assertk.assertions.key`. String literals are quoted correctly when
the IR strips the surrounding quotes.

## Source

[`MapAssertionDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/MapAssertionDetector.kt)
