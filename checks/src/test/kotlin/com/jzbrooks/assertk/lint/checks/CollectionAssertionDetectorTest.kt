package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestMode
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

        lint()
            .skipTestModes(
                TestMode.FULLY_QUALIFIED,
            ).files(kotlin(code), *ASSERTK_STUBS)
            .run()
            .expect(
                """src/clean/TestingTesting.kt:10: Warning: Use hasSize assertion [CollectionSizeAssertion]
        assertThat(list.size).isEqualTo(3)
        ~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
            )

        lint().testModes(TestMode.FULLY_QUALIFIED).files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/TestingTesting.kt:10: Warning: Use hasSize assertion [CollectionSizeAssertion]
        assertk.assertThat(list.size).isEqualTo(3)
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

        lint()
            .skipTestModes(
                TestMode.FULLY_QUALIFIED,
            ).files(kotlin(code), *ASSERTK_STUBS)
            .run()
            .expect(
                """src/clean/TestSubjectHolder.kt:12: Warning: Use hasSize assertion [CollectionSizeAssertion]
        assertThat(holder.list.size).isEqualTo(3)
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
            )

        lint().testModes(TestMode.FULLY_QUALIFIED).files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/TestSubjectHolder.kt:12: Warning: Use hasSize assertion [CollectionSizeAssertion]
        assertk.assertThat(holder.list.size).isEqualTo(3)
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `size property read is ignored without equality comparison`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isGreaterThan

            class TestingTesting {
                fun testingTest() {
                    val list: List<Int> = listOf(10, 100, 1_000)

                    assertThat(list.size).isGreaterThan(3)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectClean()
    }

    @Test
    fun `size property read is fixed`() {
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

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/TestingTesting.kt line 10: Replace size equality comparison with hasSize assertion:
@@ -4 +4
+ import assertk.assertions.hasSize
@@ -10 +11
-         assertThat(list.size).isEqualTo(3)
+         assertThat(list).hasSize(3)""",
        )
    }

    @Test
    fun `indirect size property read is fixed`() {
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

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/TestSubjectHolder.kt line 12: Replace size equality comparison with hasSize assertion:
@@ -4 +4
+ import assertk.assertions.hasSize
@@ -12 +13
-         assertThat(holder.list.size).isEqualTo(3)
+         assertThat(holder.list).hasSize(3)""",
        )
    }
}
