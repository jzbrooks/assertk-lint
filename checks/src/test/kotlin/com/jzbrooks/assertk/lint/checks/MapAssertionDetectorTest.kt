package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MapAssertionDetectorTest : LintDetectorTest() {
    override fun getDetector() = MapAssertionDetector()

    override fun getIssues() =
        listOf(
            MapAssertionDetector.ISSUE,
        )

    @Test
    fun `no issues reports clean`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.key
            import assertk.isNotNull

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map).key("9A3E6FAC-0639-4F52-8E88-D9F7512540A4").isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), kotlin(assertkStub)).run().expectClean()
    }

    @Test
    fun `direct read from map in assertion subject creation detected`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.key
            import assertk.isNotNull

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map["9A3E6FAC-0639-4F52-8E88-D9F7512540A4"]).isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), kotlin(assertkStub)).run().expect("""src/clean/TestingTesting.kt:12: Warning: Assertk map assertions provide [SuboptimalMapAssertion]
        assertThat(map["9A3E6FAC-0639-4F52-8E88-D9F7512540A4"]).isNotNull()
                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""")
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

            fun <T, U> Assert<Map<T, U>>.key(key: T): Assert<U> {

            }

            fun <T> Assert<T?>.isNotNull(): Assert<T> {

            }
            """.trimIndent()
    }
}
