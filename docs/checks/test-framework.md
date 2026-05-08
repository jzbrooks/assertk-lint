# TestFrameworkAssertionUse

**Issue ID** `TestFrameworkAssertionUse` ·
**Severity** `Warning` ·
**Category** `Correctness` ·
**Default** on

Flags assertion calls into the bundled JUnit 4, JUnit 5, or `kotlin.test`
assertion APIs when assertk is on the classpath.

## Why

Test frameworks ship their own assertions for convenience, but their failure
messages and discoverability are weaker than a fluent assertion library's.
Standardising on assertk gives you consistent output and access to richer
combinators (`prop`, `index`, `key`, …).

The detector recognises members of:

- `org.junit.Assert` (JUnit 4)
- `org.junit.jupiter.api.Assertions` (JUnit 5)
- `kotlin.test.AssertionsKt` / `kotlin.test.AssertionsKt__AssertionsKt`

## Example

=== "Flagged"

    ```kotlin
    import org.junit.Assert.assertEquals

    class TestingTesting {
        fun testingTest() {
            val first = File()
            val second = File()
            assertEquals(first, second)
        }
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isEqualTo

    class TestingTesting {
        fun testingTest() {
            val first = File()
            val second = File()
            assertThat(first).isEqualTo(second)
        }
    }
    ```

## Quick fix

Yes for **JUnit 4** and **`kotlin.test`** assertions. The fix recognises:

| Original | Replacement |
| --- | --- |
| `assertEquals(expected, actual)` | `assertThat(actual).isEqualTo(expected)` |
| `assertNotEquals(expected, actual)` | `assertThat(actual).isNotEqualTo(expected)` |
| `assertTrue(actual)` | `assertThat(actual).isTrue()` |
| `assertFalse(actual)` | `assertThat(actual).isFalse()` |
| `assertNull(actual)` | `assertThat(actual).isNull()` |
| `assertNotNull(actual)` | `assertThat(actual).isNotNull()` |
| `assertSame(expected, actual)` | `assertThat(actual).isSameAs(expected)` |
| `assertNotSame(expected, actual)` | `assertThat(actual).isNotSameAs(expected)` |
| `assertArrayEquals(expected, actual)` | `assertThat(actual).containsOnly(*expected)` |
| `assertIs<T>(value)` (kotlin.test) | `assertThat(value).isInstanceOf<T>()` |
| `assertIsNot<T>(value)` (kotlin.test) | `assertThat(value).isNotInstanceOf<T>()` |
| `fail(message)` | `assertk.fail(message)` |

If the original call carried a message argument, it is preserved as a trailing
comment so context isn't lost when the rewrite produces a shorter form.

JUnit 5 calls are flagged but not auto-fixed &mdash; the parameter ordering and
overloads vary enough that a mechanical rewrite isn't always safe.

## Source

[`TestFrameworkAssertionDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/TestFrameworkAssertionDetector.kt)
