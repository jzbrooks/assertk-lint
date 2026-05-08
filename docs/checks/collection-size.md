# CollectionSizeAssertion

**Issue ID** `CollectionSizeAssertion` ·
**Severity** `Warning` ·
**Category** `Productivity` ·
**Default** on

Flags `assertThat(collection.size).isEqualTo(n)` where assertk's `hasSize`
should be used instead.

## Why

`hasSize` is purpose-built for collection length assertions. The failure
message names the collection rather than just reporting a mismatched integer,
and the call reads more naturally. The detector specifically looks for `.size`
reads on subtypes of `java.util.Collection` followed by an `isEqualTo`
assertion from `assertk.assertions.AnyKt`.

## Example

=== "Flagged"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isEqualTo

    fun listSize() {
        val list: List<Int> = listOf(10, 100, 1_000)
        assertThat(list.size).isEqualTo(3)
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.hasSize

    fun listSize() {
        val list: List<Int> = listOf(10, 100, 1_000)
        assertThat(list).hasSize(3)
    }
    ```

The detector also catches indirect reads:

```kotlin
data class Holder(val list: List<Int>)

assertThat(holder.list.size).isEqualTo(3) // flagged
```

## Quick fix

Yes &mdash; the IDE rewrites the subject from `collection.size` to
`collection`, replaces `isEqualTo(n)` with `hasSize(n)`, and adds the
`assertk.assertions.hasSize` import.

## Source

[`CollectionAssertionDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/CollectionAssertionDetector.kt)
