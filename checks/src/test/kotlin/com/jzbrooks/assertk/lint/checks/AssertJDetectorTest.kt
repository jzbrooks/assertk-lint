package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AssertJDetectorTest : LintDetectorTest() {
    override fun getDetector() = AssertJDetector()

    override fun getIssues() =
        listOf(
            AssertJDetector.ISSUE.setEnabledByDefault(true),
        )

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
            ).run()
            .expectClean()
    }

    @Test
    fun `assertj assertion not reported in java source`() {
        val code =
            """
            package error;

            import java.io.File;
            import org.assertj.core.api.Assertions.assertThat;

            class TestingTesting {
                fun testingTest() {
                    val first = File();
                    val second = File();
                    assertThat(first).isEqualTo(second);
                }
            }
            """.trimIndent()

        lint()
            .files(
                java(code),
                java(ASSERTJ_STUB),
                java(ASSERTION_STUB),
            ).run()
            .expectClean()
    }

    @Test
    fun `assertj assertion detected`() {
        val code =
            """
            package error

            import java.io.File
            import org.assertj.core.api.Assertions.assertThat

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()
                    assertThat(first).isEqualTo(second)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                java(ASSERTJ_STUB),
                java(ASSERTION_STUB),
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:10: Warning: Use assertk assertions [AssertJUse]
        assertThat(first).isEqualTo(second)
        ~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
            )
    }

    companion object {
        private const val ASSERTJ_STUB = """
            package org.assertj.core.api;

            public class Assertions {
                public static Assertion assertThat(Object actual) {
                    return new Assertion();
                }
            }
        """

        private const val ASSERTION_STUB = """
            package org.assertj.core.api;

            public class Assertion {
                public void isEqualTo(Object expected) {
                }
            }
        """
    }
}
