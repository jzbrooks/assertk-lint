package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TryCatchDetectorTest : LintDetectorTest() {
    override fun getDetector() = TryCatchDetector()

    override fun getIssues() = listOf(TryCatchDetector.ISSUE)

    @Test
    fun `assertion in catch block detected`() {
        val code =
            """
            package clean

            import risky.RotatingDetonationEngine
            import assertk.fail
            import assertk.assertThat
            import assertk.assertions.isEqualTo
            import assertk.assertions.prop

            class Testing {
                fun unreliableTest() {
                    val engine = RotatingDetonationEngine()
                    try {
                        engine.rotate()
                        fail() // ignored in expression count
                    } catch (e: Exception) {
                        assertThat(e).prop(Exception::message).isEqualTo("Boom!")
                    }
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                kotlin(throwingStub),
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:12: Warning: Use assertk.assertFailure [TryCatchAssertion]
        try {
        ^
0 errors, 1 warnings""",
            )
    }

    @Test
    fun `assertion in catch block detected without fail call in try block`() {
        val code =
            """
            package clean

            import risky.RotatingDetonationEngine
            import assertk.fail
            import assertk.assertThat
            import assertk.assertions.isEqualTo
            import assertk.assertions.prop

            class Testing {
                fun unreliableTest() {
                    val engine = RotatingDetonationEngine()
                    try {
                        engine.rotate()
                    } catch (e: Exception) {
                        assertThat(e).prop(Exception::message).isEqualTo("Boom!")
                    }
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                kotlin(throwingStub),
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:12: Warning: Use assertk.assertFailure [TryCatchAssertion]
        try {
        ^
0 errors, 1 warnings""",
            )
    }

    @Test
    fun `multiple assertions in catch block detected`() {
        val code =
            """
            package clean

            import risky.RotatingDetonationEngine
            import assertk.fail
            import assertk.assertThat
            import assertk.assertions.isEqualTo
            import assertk.assertions.isInstanceOf
            import assertk.assertions.prop
            import java.lang.IllegalStateException

            class Testing {
                fun unreliableTest() {
                    val engine = RotatingDetonationEngine()
                    try {
                        engine.rotate()
                        fail()
                    } catch (e: Exception) {
                        assertThat(e).prop(Exception::message).isEqualTo("Boom!")
                        assertThat(e).isInstanceOf(IllegalStateException::class)
                    }
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                kotlin(throwingStub),
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:14: Warning: Use assertk.assertFailure [TryCatchAssertion]
        try {
        ^
0 errors, 1 warnings""",
            )
    }

    @Test
    fun `assertion with complicated try block ignored`() {
        val code =
            """
            package clean

            import risky.RotatingDetonationEngine
            import assertk.fail
            import assertk.assertThat
            import assertk.assertions.isEqualTo
            import assertk.assertions.prop

            class Testing {
                fun unreliableTest() {
                    val engine = RotatingDetonationEngine()
                    try {
                        engine.rotate()
                        println(java.lang.System.currentTimeMillis())
                        fail()
                    } catch (e: Exception) {
                        assertThat(e).prop(Exception::message).isEqualTo("Boom!")
                    }
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                kotlin(throwingStub),
                *ASSERTK_STUBS,
            ).run()
            .expectClean()
    }

    @Test
    fun `assertion with empty catch block ignored`() {
        val code =
            """
            package clean

            import risky.RotatingDetonationEngine
            import assertk.fail
            import assertk.assertThat
            import assertk.assertions.isEqualTo
            import assertk.assertions.prop

            class Testing {
                fun unreliableTest() {
                    val engine = RotatingDetonationEngine()
                    try {
                        engine.rotate()
                        fail()
                    } catch (_: Exception) {
                    }
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                kotlin(throwingStub),
                *ASSERTK_STUBS,
            ).run()
            .expectClean()
    }

    @Test
    fun `assertion with complicated catch block ignored`() {
        val code =
            """
            package clean

            import risky.RotatingDetonationEngine
            import assertk.fail
            import assertk.assertThat
            import assertk.assertions.isEqualTo
            import assertk.assertions.prop

            class Testing {
                fun unreliableTest() {
                    val engine = RotatingDetonationEngine()
                    try {
                        engine.rotate()
                        fail()
                    } catch (e: Exception) {
                        println(java.lang.System.currentTimeMillis())
                        assertThat(e).prop(Exception::message).isEqualTo("Boom!")
                    }
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                kotlin(throwingStub),
                *ASSERTK_STUBS,
            ).run()
            .expectClean()
    }

    companion object {
        val throwingStub =
            """
            package risky

            class RotatingDetonationEngine {
                fun rotate() {
                    if (System.currentTimeMillis() % 2 == 0) {
                        throw Exception("Boom!")
                    }
                }
            }
            """.trimIndent()
    }
}
