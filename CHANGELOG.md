# Changelog

## Unreleased

### Fixed

- Detectors avoid signaling failures in production code
- Quick fixes for _TestFrameworkAssertionDetector_ relocate `message` assertion parameters to comments in a more robust manner

## 1.5.0 - 2025-02-16

### Added
- _KotlinAssertionDetector_ prohibits use of `kotlin.assert` in tests
- Quick fixes for null check and equality tests for _UnusedAssertkAssertion_
- Quick fixes for common junit 4 assertions detected by _TestFrameworkAssertionDetector_
- Quick fixes for common kotlin.test assertions cases detected by _TestFrameworkAssertionDetector_

### Fixed
- Fully qualified function call handling is improved across several detectors

## 1.4.0 - 2024-12-23

### Added

- _EqualityComparisonAssertion_ prohibits equality expression in `assertThat`
- _CollectionSizeAssertion_ prohibits `Collection.size` reads in `assertThat`

### Fixed

- _UnusedAssertkAssertion_ flags _used_ assertion subjects in lambda expressions

## 1.3.0 - 2024-10-22

### Added

- _NullComparisonAssertion_ requires using assertk's `isNotNull` and `isNull` assertions for null assertions
- _UseIndexAssertion_ requires using assertk's indexing assertions (`index`, `first`, etc) to make assertions on a particular array or list value

## 1.2.1 - 2024-07-05

### Fixed

- _MapValueAssertion_ no longer raises issues on array indexing expressions as an `assertThat` argument

## 1.2.0 - 2024-06-27

### Added

- _MapValueAssertion_ requires using assertk's `key` function to make assertions on a particular map value
- _KeySetAbsentAssertion_ requires using assertk's `doesNotContainKey` function to assert a key is absent in a map
- _KeySetPresentAssertion_ requires using assertk's `key` function to assert a key is present in a map
- _TryCatchAssertion_ requires using assertk's `assertFailure` function to handle expected exceptions

## 1.1.1 - 2024-01-02

### Fixed

- Updated detector scope to avoid requiring `checkTestSources`
- kotlin.test assertions were not detected by _TestFrameworkAssertionDetector_

## 1.1.0 - 2023-11-26

### Added

- _UnusedAssertkAssertion_ detects unused assertion subjects

## 1.0.1 - 2023-11-18

### Fixed

- Jar metadata pointed to an incorrect lint issue registry class

## 1.0.0 - 2023-11-18

### Added

- _TestFrameworkAssertionUse_ detects junit4, junit5, and kotlin.test assertions
- _GoogleTruthUse_ detects Google Truth assertions (opt-in)
- _AssertJUse_ detects AssertJ assertions (opt-in)
