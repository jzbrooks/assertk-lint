package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UnusedAssertionDetectorTest : LintDetectorTest() {
    override fun getDetector() = UnusedAssertionDetector()

    override fun getIssues() = listOf(UnusedAssertionDetector.ISSUE)

    @Test
    fun `subjects with assertions inside lambdas should report clean`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.isNotNull

            enum class Scenario(assertion: () -> Unit) {
                LambdaAssertionScenario(assertion = { assertThat("").isNotNull() })
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectClean()
    }

    @Test
    fun `subjects without assertions inside lambdas should be detected`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat

            enum class Scenario(assertion: () -> Unit) {
                LambdaAssertionScenario(assertion = { assertThat("") })
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/Scenario.kt:7: Error: Assertion subjects without assertions never fail a test [UnusedAssertkAssertion]
    LambdaAssertionScenario(assertion = { assertThat("") })
                                          ~~~~~~~~~~~~~~
1 errors, 0 warnings""",
        )
    }

    @Test
    fun `no issues reports clean`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()

                    assertThat(first).isEqualTo(second)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectClean()
    }

    @Test
    fun `clean with assignment`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()

                    assertThat(first).apply {
                        isEqualTo(second)
                    }
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectClean()
    }

    @Test
    fun `unused assertThat is detected`() {
        val code =
            """
            package error

            import java.io.File
            import assertk.assertThat

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()

                    assertThat(first)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/error/TestingTesting.kt:11: Error: Assertion subjects without assertions never fail a test [UnusedAssertkAssertion]
        assertThat(first)
        ~~~~~~~~~~~~~~~~~
1 errors, 0 warnings""",
        )
    }
}
