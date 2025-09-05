package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class KotlinAssertionDetectorTest : LintDetectorTest() {
    override fun getDetector() = KotlinAssertionDetector()

    override fun getIssues() = listOf(KotlinAssertionDetector.ISSUE)

    @Test
    fun `no issues reports clean`() {
        val code =
            """
            package error

            import java.io.File

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                kotlin(
                    """
            package kotlin
            class PreconditionsKt__AssertionsJVMKt {
               fun assert(condition: Boolean) {}
            }""",
                ),
            ).run()
            .expectClean()
    }

    @Test
    fun `kotlin assertion detected`() {
        val code =
            """
            package error

            import java.io.File

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    assert(first.canRead)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                kotlin(
                    """
            package kotlin
            class PreconditionsKt__AssertionsJVMKt {
               fun assert(condition: Boolean) {}
            }""",
                ),
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:8: Warning: Kotlin assertion is used [KotlinAssertionUse]
        assert(first.canRead)
        ~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
            )
    }

    @Test
    fun `kotlin assertion fixed`() {
        val code =
            """
            package error

            import java.io.File

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    assert(first.canRead)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                kotlin(
                    """
                    package kotlin
                    class PreconditionsKt__AssertionsJVMKt {
                       fun assert(condition: Boolean) {}
                    }
                    """.trimIndent(),
                ),
            ).run()
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 8: Replace with assertThat(first.canRead).isTrue():
@@ -3 +3
+ import assertk.assertThat
+ import assertk.assertions.isTrue
@@ -8 +10
-         assert(first.canRead)
+         assertThat(first.canRead).isTrue()""",
            )
    }
}
