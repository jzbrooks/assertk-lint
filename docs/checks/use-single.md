# UseSingleAssertion

**Issue ID** `UseSingleAssertion` ·
**Severity** `Warning` ·
**Category** `Productivity` ·
**Default** on

Flags the `assertk.all { hasSize(1); first()... }` (and the `index(0)`
equivalent) pattern where assertk's `single()` should be used.

## Why

`Assert<List<T>>.single()` asserts that the collection has exactly one element
*and* transforms the subject to that element. Doing it by hand &mdash;
`hasSize(1)` plus `first()`/`index(0)` inside an `all { }` block &mdash; is
two assertions where one will do, and the chained form reads more naturally.

The detector fires only on the simple two-statement shape inside an
`all { }` block from `assertk`:

- `hasSize(1)` paired with `first().<something>`, **or**
- `hasSize(1)` paired with `index(0).<something>`.

The order doesn't matter. Anything else &mdash; `hasSize(2)`, `index(1)`,
extra statements in the block &mdash; is left alone.

## Example

=== "Flagged"

    ```kotlin
    import assertk.all
    import assertk.assertThat
    import assertk.assertions.hasSize
    import assertk.assertions.index
    import assertk.assertions.isEqualTo

    fun onlyElement() {
        val list: List<String> = listOf("a")
        assertThat(list).all {
            hasSize(1)
            index(0).isEqualTo("a")
        }
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isEqualTo
    import assertk.assertions.single

    fun onlyElement() {
        val list: List<String> = listOf("a")
        assertThat(list).single().isEqualTo("a")
    }
    ```

The `first()` form is recognised the same way:

```kotlin
assertThat(list).all {
    hasSize(1)
    first().isEqualTo("a") // also flagged
}
```

## Quick fix

Yes &mdash; the IDE collapses the entire `all { }` block to
`single().<chained assertion>`, preserving whatever assertion was applied to
the element, and adds the `assertk.assertions.single` import.

## Source

[`SingleAssertionDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/SingleAssertionDetector.kt)
