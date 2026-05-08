# Configuration

Most assertk-lint checks are enabled by default. A few aren't &mdash; the
"competing library" detectors are opt-in so the plugin doesn't second-guess
projects that ship multiple assertion libraries on purpose.

## Opt-in checks

| Issue ID | Default |
| --- | --- |
| [`AssertJUse`](checks/assertj.md) | off |
| [`GoogleTruthUse`](checks/google-truth.md) | off |

Enable them through your project's `lint.xml` (or via the Lint DSL):

```xml title="lint.xml"
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <issue id="AssertJUse" severity="warning" />
    <issue id="GoogleTruthUse" severity="warning" />
</lint>
```

```kotlin title="build.gradle.kts"
android {
    lint {
        enable += setOf("AssertJUse", "GoogleTruthUse")
    }
}
```

## Promoting warnings to errors

If you want, say, `TestFrameworkAssertionUse` to fail the build instead of
warning:

```xml title="lint.xml"
<lint>
    <issue id="TestFrameworkAssertionUse" severity="error" />
</lint>
```

Or with the DSL:

```kotlin title="build.gradle.kts"
android {
    lint {
        error += "TestFrameworkAssertionUse"
    }
}
```

## Disabling a check

Disable a single check the same way:

```xml title="lint.xml"
<lint>
    <issue id="EqualityComparisonAssertion" severity="ignore" />
</lint>
```

You can also suppress at the call site with `@Suppress("EqualityComparisonAssertion")`
or, more idiomatically for Lint, `@SuppressLint("EqualityComparisonAssertion")`.

## Running

```bash
./gradlew lint
```

Reports land in `build/reports/lint-results-*.html` (and `.xml`). The HTML
report deep-links to each check's documentation.

!!! info "All issue IDs"
    See the [Checks overview](checks/index.md) for the full list of issue IDs,
    severities, and defaults.
