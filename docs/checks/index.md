# Checks

assertk-lint ships **15 issues** across 12 detectors. All detectors run only
against Kotlin test sources.

## Errors

These are the only checks that fire as `Severity.ERROR`. They flag patterns
where the surrounding test will silently pass even though it shouldn't.

| Issue ID | What it flags | Quick fix |
| --- | --- | --- |
| [`UnusedAssertkAssertion`](unused-assertion.md) | An `assertThat(...)` chain that never has an assertion called on it | Partial &mdash; for null/equality/`is`-check subjects |
| [`AssertThatEqualsUsage`](assert-that-equals.md) | `equals()` called on an `assertk.Assert` receiver (returns a `Boolean`, never asserts) | Yes |

## Warnings (on by default)

| Issue ID | What it flags | Quick fix |
| --- | --- | --- |
| [`TestFrameworkAssertionUse`](test-framework.md) | JUnit 4 / JUnit 5 / `kotlin.test` assertion calls | JUnit 4 + `kotlin.test` |
| [`KotlinAssertionUse`](kotlin-assertion.md) | `kotlin.assert(...)` in tests | Yes |
| [`NullComparisonAssertion`](null-comparison.md) | `assertThat(x == null)` / `assertThat(x != null)` | Yes |
| [`EqualityComparisonAssertion`](equality-comparison.md) | `assertThat(a == b)` / `assertThat(a != b)` | Yes |
| [`CollectionSizeAssertion`](collection-size.md) | `assertThat(list.size).isEqualTo(n)` &mdash; use `hasSize(n)` | Yes |
| [`UseIndexAssertion`](use-index.md) | `assertThat(list[i])` &mdash; use `assertThat(list).index(i)` | Yes |
| [`UseSingleAssertion`](use-single.md) | `all { hasSize(1); first()... }` &mdash; use `single()` | Yes |
| [`MapValueAssertion`](map-value.md) | `assertThat(map[k])` &mdash; use `assertThat(map).key(k)` | Yes |
| [`KeySetPresentAssertion`](key-set-present.md) | `assertThat(map.keys).contains(k)` &mdash; use `key(k)` | Yes |
| [`KeySetAbsentAssertion`](key-set-absent.md) | `assertThat(map.keys).doesNotContain(k)` &mdash; use `doesNotContainKey(k)` | Yes |
| [`TryCatchAssertion`](try-catch.md) | A `try { ... } catch { assertThat(e)... }` block whose only purpose is exception assertion &mdash; use `assertFailure` | No |

## Opt-in

Off by default; turn them on in `lint.xml` (see [Configuration](../configuration.md))
when your project is committed to a single assertion library.

| Issue ID | What it flags | Quick fix |
| --- | --- | --- |
| [`AssertJUse`](assertj.md) | AssertJ assertion calls in Kotlin tests | No |
| [`GoogleTruthUse`](google-truth.md) | Google Truth assertion calls in Kotlin tests | No |

## How to read each page

Every check page lists:

- **Issue ID** &mdash; the stable identifier you'd put in `lint.xml`,
  `@SuppressLint(...)`, or the IDE.
- **Severity / Category / Default** &mdash; from the detector's `Issue.create(...)`.
- **Why** &mdash; the rationale and the assertk equivalent.
- **Example** &mdash; tabs showing the flagged code and the preferred form.
- **Quick fix** &mdash; whether the IDE can mechanically rewrite for you.
- **Source** &mdash; a link to the detector implementation on GitHub.
