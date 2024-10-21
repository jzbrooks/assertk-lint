package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class IndexDetectorTest : LintDetectorTest() {
    override fun getDetector() = IndexDetector()

    override fun getIssues() =
        listOf(
            IndexDetector.ISSUE,
        )

    @Test
    fun `index assertion reports clean`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.index
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val array = arrayOf(10, 100, 1_000)

                    assertThat(array).index(2).isEqualTo(1_000)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectClean()
    }

    @Test
    fun `index expression as subject is reported`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.index
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val array = arrayOf(10, 100, 1_000)

                    assertThat(array[2]).isEqualTo(1_000)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """
            src/clean/TestingTesting.kt:11: Warning: Index with assertk assertions [CollectionIndexAssertion]
                    assertThat(array[2]).isEqualTo(1_000)
                               ~~~~~~~~
            0 errors, 1 warnings
            """.trimIndent(),
        )
    }
}
