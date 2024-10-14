package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AnyAssertionDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = AnyAssertionDetector()

    override fun getIssues() = listOf(AnyAssertionDetector.NULL_CHECK_ISSUE)

    @Test
    fun `null check in assertThat detected`() {
        val code =
            """
            package clean

            import kotlin.text.ifEmpty
            import assertk.fail
            import assertk.assertThat
            import assertk.assertions.isEqualTo
            import assertk.assertions.prop

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name == null).isFalse()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/Testing.kt:12: Warning: Use Assert<Any?>.isNull or Assert<Any?>.isNotNull to assert against nullability [NullCheckAssertion]
        assertThat(name == null).isFalse()
        ~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }
}
