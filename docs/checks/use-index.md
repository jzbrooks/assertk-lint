# UseIndexAssertion

**Issue ID** `UseIndexAssertion` ·
**Severity** `Warning` ·
**Category** `Correctness` ·
**Default** on

Flags `assertThat(collection[i])` and `assertThat(array[i])` where assertk's
`index` (and friends like `first()`) should be used.

## Why

Indexing the collection inside `assertThat` throws an
`IndexOutOfBoundsException` at the assertion site &mdash; the failure looks
like a crash, not an assertion failure. `Assert<List<T>>.index(i): Assert<T>`
asserts that the element exists *and* transforms the subject so you can chain
further checks on the value.

The detector recognises both `Array` and `java.util.List` (and subtypes) as
indexable. Reads through index that are *not* the `assertThat` argument &mdash;
e.g. `assertThat(array[1].sign).isEqualTo(1)` &mdash; are intentionally not
flagged.

## Example

=== "Flagged"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isEqualTo

    fun thirdElement() {
        val array = arrayOf(10, 100, 1_000)
        assertThat(array[2]).isEqualTo(1_000)
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.index
    import assertk.assertions.isEqualTo

    fun thirdElement() {
        val array = arrayOf(10, 100, 1_000)
        assertThat(array).index(2).isEqualTo(1_000)
    }
    ```

## Quick fix

Yes &mdash; the IDE rewrites
`assertThat(array[i])` to `assertThat(array).index(i)` and adds the
`assertk.assertions.index` import.

## Source

[`IndexDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/IndexDetector.kt)
