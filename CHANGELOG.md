# Changelog

## Unreleased

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
