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
            import assertk.isEqualTo

            enum class Scenario(assertion: () -> Unit) {
                LambdaAssertionScenario(assertion = { assertThat("").isNotNull() })
            }
            """.trimIndent()

        lint().files(kotlin(code), kotlin(assertkStub)).run().expectClean()
    }

    @Test
    fun `subjects without assertions inside lambdas should be detected`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.isEqualTo

            enum class Scenario(assertion: () -> Unit) {
                LambdaAssertionScenario(assertion = { assertThat("") })
            }
            """.trimIndent()

        lint().files(kotlin(code), kotlin(assertkStub)).run().expect(
            """src/clean/Scenario.kt:8: Error: Assertion subjects without assertions never fail a test [UnusedAssertkAssertion]
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
            import assertk.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()

                    assertThat(first).isEqualTo(second)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), kotlin(assertkStub)).run().expectClean()
    }

    @Test
    fun `clean with assignment`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.isEqualTo

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

        lint().files(kotlin(code), kotlin(assertkStub)).run().expectClean()
    }

    @Test
    fun `unused assertThat is detected`() {
        val code =
            """
            package error

            import java.io.File
            import assertk.assertThat
            import assertk.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()

                    assertThat(first)
                }
            }
            """.trimIndent()

        lint().files(kotlin(assertkStub), kotlin(code)).run().expect(
            """src/error/TestingTesting.kt:12: Error: Assertion subjects without assertions never fail a test [UnusedAssertkAssertion]
        assertThat(first)
        ~~~~~~~~~~~~~~~~~
1 errors, 0 warnings""",
        )
    }

    companion object {
        val assertkStub =
            """
            package assertk

            // This name is a hack to get the test infractructure to correctly
            // name this test stub file's class to AssertkKt
            class Assert<T> {

            }

            fun <T> assertThat(subject: T?): Assert<T> {

            }

            fun <T> Assert<T>.isEqualTo(expected: T) {

            }
            """.trimIndent()
    }
}
