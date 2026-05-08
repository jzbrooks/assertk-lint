---
hide:
  - navigation
  - toc
---

<div class="hero" markdown>

# assertk-lint

<p class="lead">
A set of Android Lint rules to encourage proper use of
<a href="https://github.com/willowtreeapps/assertk">assertk</a> &mdash; catch
common assertion mistakes at compile time and steer tests toward fluent,
readable, and reliable assertions.
</p>

[Install :material-arrow-right:](install.md){ .md-button .md-button--primary }
[Browse the checks](checks/index.md){ .md-button }

</div>

<div class="grid cards" markdown>

-   :material-rocket-launch:{ .lg } **Drop-in**

    ---

    One line in `build.gradle.kts`. Works on Android *and* JVM modules with the
    `com.android.lint` plugin. No detector configuration required for the
    default checks.

    [:octicons-arrow-right-24: Install](install.md)

-   :material-shield-check:{ .lg } **Catches what tests miss**

    ---

    Two of the checks fire as **errors** &mdash; assertion subjects without
    assertions, and `equals()` calls on assertion subjects &mdash; because the
    surrounding test will silently pass either way.

    [:octicons-arrow-right-24: Errors](checks/index.md#errors)

-   :material-tools:{ .lg } **IDE quick fixes**

    ---

    Most checks ship a quick fix. Replace JUnit/`kotlin.test`/`assert(...)`
    calls with the assertk equivalent without leaving the editor.

    [:octicons-arrow-right-24: Reference](checks/index.md)

-   :material-package-variant:{ .lg } **On Maven Central**

    ---

    Published as `com.jzbrooks:assertk-lint`. MIT licensed. Issue IDs are
    stable; new checks are introduced behind explicit version bumps.

    [:octicons-arrow-right-24: Changelog](changelog.md)

</div>
