# Contributing

Bug reports, ideas for new checks, and pull requests are welcome &mdash; file
them on [GitHub](https://github.com/jzbrooks/assertk-lint/issues).

## Building

```bash
./gradlew check
```

That runs the unit tests for every detector and the Spotless / ktlint format
checks. To auto-fix formatting:

```bash
./gradlew spotlessApply
```

## Adding a new check

A check is one Kotlin file in
`checks/src/main/kotlin/com/jzbrooks/assertk/lint/checks/` plus a test in the
mirror directory under `src/test/kotlin/`.

The shape is:

```kotlin
class MyDetector : Detector(), Detector.UastScanner {
    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        if (!context.isTestSource) return null
        if (context.uastFile?.lang != KotlinLanguage.INSTANCE) return null

        return object : UElementHandler() {
            override fun visitCallExpression(node: UCallExpression) {
                // ... report on node when conditions match
            }
        }
    }

    companion object {
        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "MyAssertion",
            briefDescription = "...",
            explanation = "...",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                MyDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
            ),
        )
    }
}
```

Then register it in `AssertkIssueRegistry`:

```kotlin
override val issues = listOf(
    // existing entries...
    MyDetector.ISSUE,
)
```

A few conventions in this codebase:

- **Test sources only.** Every detector bails early on `!context.isTestSource`.
  Production code is never flagged.
- **Kotlin only.** Each detector also bails on
  `context.uastFile?.lang != KotlinLanguage.INSTANCE`. Java tests are out of
  scope.
- **Stable issue IDs.** Once an issue ID ships, don't rename it &mdash; it
  becomes part of consumers' `lint.xml` and IDE suppression annotations.
- **Quick fixes are first-class.** Where a mechanical rewrite is unambiguous,
  attach a `LintFix` so the IDE can apply it. See `TestFrameworkAssertionDetector`
  for the most thorough example.
- **Mirror real assertk and JUnit shapes in tests.** The fixture stubs live in
  `checks/src/test/kotlin/.../Stubs.kt`; reuse them.

## Adding documentation for a new check

When you add a detector, also add a page under `docs/checks/` following the
template used by the existing pages (heading + metadata line + Why + Example
tabs + Quick fix + Source link), and add the page to the `nav:` block in
`mkdocs.yml`.

## Releasing

Maintainer flow:

1. Update `CHANGELOG.md` &mdash; move `Unreleased` content into a new dated
   section.
2. Tag and publish a GitHub release. The `publish.yml` workflow handles Maven
   Central.
3. The post-release CI then opens a PR re-adding the empty `Unreleased`
   skeleton.
