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
    fun `array index expression as subject is reported`() {
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
            src/clean/TestingTesting.kt:11: Warning: Index with assertk assertions [UseIndexAssertion]
                    assertThat(array[2]).isEqualTo(1_000)
                               ~~~~~~~~
            0 errors, 1 warnings
            """.trimIndent(),
        )
    }

    @Test
    fun `list index expression as subject is reported`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val list = listOf(10, 100, 1_000)

                    assertThat(list[2]).isEqualTo(1_000)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """
            src/clean/TestingTesting.kt:10: Warning: Index with assertk assertions [UseIndexAssertion]
                    assertThat(list[2]).isEqualTo(1_000)
                               ~~~~~~~
            0 errors, 1 warnings
            """.trimIndent(),
        )
    }

    @Test
    fun `list implementer index expression as subject is reported`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isEqualTo

            class CrazyWrapper(private val list: List<Int>): List<Int> by list

            class TestingTesting {
                fun testingTest() {
                    val weirdList = CrazyWrapper(listOf(10, 100, 1_000))

                    assertThat(weirdList[2]).isEqualTo(1_000)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """
            src/clean/CrazyWrapper.kt:12: Warning: Index with assertk assertions [UseIndexAssertion]
                    assertThat(weirdList[2]).isEqualTo(1_000)
                               ~~~~~~~~~~~~
            0 errors, 1 warnings
            """.trimIndent(),
        )
    }

    @Test
    fun `array index expression as subject is fixed`() {
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

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """
Fix for src/clean/TestingTesting.kt line 11: Replace with assertThat(array).index(2):
@@ -11 +11
-         assertThat(array[2]).isEqualTo(1_000)
+         assertThat(array).index(2).isEqualTo(1_000)
            """.trimIndent(),
        )
    }
}
