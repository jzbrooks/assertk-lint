# GoogleTruthUse

**Issue ID** `GoogleTruthUse` ·
**Severity** `Warning` ·
**Category** `Correctness` ·
**Default** **off** (opt-in)

Flags assertion calls into Google Truth (`com.google.common.truth.Truth`) when
the project has standardised on assertk. **Off by default** &mdash; turn it on
explicitly if you want to reject Truth in this codebase.

## Why

Truth and assertk fill the same niche. A codebase that mixes both makes every
new test a small style decision. Where the convention is assertk, this check
enforces it.

## Enabling

```xml title="lint.xml"
<lint>
    <issue id="GoogleTruthUse" severity="warning" />
</lint>
```

```kotlin title="build.gradle.kts"
android {
    lint {
        enable += "GoogleTruthUse"
    }
}
```

## Example

=== "Flagged"

    ```kotlin
    import com.google.common.truth.Truth.assertThat

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

Not provided. Replace the import manually &mdash; `assertk.assertThat` plus the
specific `assertk.assertions.*` calls you need.

## Source

[`GoogleTruthDetector.kt`](https://github.com/jzbrooks/assertk-lint/blob/master/checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/GoogleTruthDetector.kt)
