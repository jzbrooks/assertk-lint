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
    fun `hasSize assertion reports clean`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.hasSize

            class TestingTesting {
                fun testingTest() {
                    val list: List<Int> = listOf(10, 100, 1_000)

                    assertThat(list).hasSize(3)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectClean()
    }

    @Test
    fun `size property read is detected`() {
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

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/TestingTesting.kt:10: Warning: Use hasSize assertion [CollectionSizeAssertion]
        assertThat(list.size).isEqualTo(3)
        ~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `indirect size property read is detected`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isEqualTo

            data class TestSubjectHolder(val list: List<Int>)

            class TestingTesting {
                fun testingTest() {
                    val holder = TestSubjectHolder(listOf(10, 100, 1_000))

                    assertThat(holder.list.size).isEqualTo(3)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/TestSubjectHolder.kt:12: Warning: Use hasSize assertion [CollectionSizeAssertion]
        assertThat(holder.list.size).isEqualTo(3)
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }
}
