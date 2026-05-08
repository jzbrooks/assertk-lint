# AssertJUse

**Issue ID** `AssertJUse` ·
**Severity** `Warning` ·
**Category** `Correctness` ·
**Default** **off** (opt-in)

Flags assertion calls into AssertJ (`org.assertj.core.api.Assertions`) when the
project has standardised on assertk. **Off by default** &mdash; turn it on
explicitly if you want to reject AssertJ in this codebase.

## Why

AssertJ and assertk overlap heavily, and mixing them in the same test suite
gives readers a "which library do I use?" decision on every test. If the
project's convention is assertk, this check enforces it.

## Enabling

```xml title="lint.xml"
<lint>
    <issue id="AssertJUse" severity="warning" />
</lint>
```

```kotlin title="build.gradle.kts"
android {
    lint {
        enable += "AssertJUse"
    }
}
```

## Example

=== "Flagged"

    ```kotlin
    import org.assertj.core.api.Assertions.assertThat

    fun test() {
        assertThat(name).isEqualTo("Ada")
    }
    ```

=== "Preferred"

    ```kotlin
    import assertk.assertThat
    import assertk.assertions.isEqualTo

    fun test() {
        assertThat(name).isEqualTo("Ada")
    }
    ```

## Quick fix

Not provided. The two libraries' assertion shapes look similar but their import
graphs and edge-case behaviour differ enough that a mechanical rewrite isn't
safe. Replace imports manually.

## Source

[`AssertJDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/AssertJDetector.kt)
