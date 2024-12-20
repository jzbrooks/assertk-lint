package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CollectionAssertionDetectorTest : LintDetectorTest() {
    override fun getDetector() = CollectionAssertionDetector()

    override fun getIssues() =
        listOf(
            CollectionAssertionDetector.SIZE_READ_ISSUE,
        )

    @Test
    fun `index assertion reports clean`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val list: List<Int> = listOf(10, 100, 1_000)

                    assertThat(list.size).isEqualTo(3)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectClean()
    }
}
