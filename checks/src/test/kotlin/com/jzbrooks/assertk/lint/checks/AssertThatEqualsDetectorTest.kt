package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AssertThatEqualsDetectorTest : LintDetectorTest() {
    override fun getDetector() = AssertThatEqualsDetector()

    override fun getIssues() = listOf(AssertThatEqualsDetector.ISSUE)

    @Test
    fun `reports assertThat equals usage`() {
        val code =
            """
            package clean

            import assertk.assertThat

            fun test() {
                assertThat(1).equals(1)
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:6: Error: Replace Any.equals with Assert.isEqualTo [AssertThatEqualsUsage]
    assertThat(1).equals(1)
    ~~~~~~~~~~~~~~~~~~~~~~~
1 error""",
            )
    }

    @Test
    fun `does not report isEqualTo usage`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isEqualTo

            fun test() {
                assertThat(1).isEqualTo(1)
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expectClean()
    }

    @Test
    fun `does not report non assertk equals usage`() {
        val code =
            """
            package clean

            class Wrapper {
                fun equals(other: Any?) = true
            }

            fun assertThat(value: Int) = Wrapper()

            fun test() {
                assertThat(1).equals(1)
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
    fun `reports fully qualified assertk call`() {
        val code =
            """
            package clean

            fun test() {
                assertk.assertThat(1).equals(1)
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:4: Error: Replace Any.equals with Assert.isEqualTo [AssertThatEqualsUsage]
    assertk.assertThat(1).equals(1)
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
1 error""",
            )
    }

    @Test
    fun `reports chained assertk assertion equals usage`() {
        val code =
            """
            package clean

            import assertk.assertThat

            fun test() {
                val tmp: assertk.Assert<Int> = assertThat(1)
                    .isNotNull()
                    .isInstanceOf(Int::class.java)
                tmp.equals(1)
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:9: Error: Replace Any.equals with Assert.isEqualTo [AssertThatEqualsUsage]
    tmp.equals(1)
    ~~~~~~~~~~~~~
1 error""",
            )
    }

    @Test
    fun `reports equals on Assert value`() {
        val code =
            """
            package clean
            import assertk.assertThat

            fun test() {
                val item = assertThat(1)
                item.equals(1)
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:6: Error: Replace Any.equals with Assert.isEqualTo [AssertThatEqualsUsage]
    item.equals(1)
    ~~~~~~~~~~~~~~
1 error""",
            )
    }

    @Test
    fun `reports equals inside scope lambda on assertk receiver`() {
        val code =
            """
            package clean
            import assertk.assertThat

            fun test() {
                assertThat(1).run {
                    equals(1)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:6: Error: Replace Any.equals with Assert.isEqualTo [AssertThatEqualsUsage]
        equals(1)
        ~~~~~~~~~
1 error""",
            )
    }

    @Test
    fun `quick fix replaces equals with isEqualTo`() {
        val code =
            """
            package clean

            import assertk.assertThat

            fun test() {
                assertThat(1).equals(1)
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 6: Replace with isEqualTo(1):
@@ -3,0 +4 @@
+import assertk.assertions.isEqualTo
@@ -6 +7 @@
-    assertThat(1).equals(1)
+    assertThat(1).isEqualTo(1)""",
            )
    }

    @Test
    fun `does not report extension function named equals returning unit`() {
        val code =
            """
            package clean

            import assertk.Assert
            import assertk.assertThat

            fun Assert<Int>.equals(somethingElse: Int) {

            }

            fun test() {
                assertThat(1).equals(somethingElse = 1)
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expectClean()
    }
}
