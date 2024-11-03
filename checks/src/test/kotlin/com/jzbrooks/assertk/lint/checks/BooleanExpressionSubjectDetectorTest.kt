package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BooleanExpressionSubjectDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = BooleanExpressionSubjectDetector()

    override fun getIssues() =
        listOf(
            BooleanExpressionSubjectDetector.NULL_EXPR_ISSUE,
            BooleanExpressionSubjectDetector.EQUALITY_EXPR_ISSUE,
        )

    @Test
    fun `clean null check`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isNotNull

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name).isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectClean()
    }

    @Test
    fun `null check in assertThat detected`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isFalse

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name == null).isFalse()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/Testing.kt:9: Warning: Use built-in nullability assertions [NullComparisonAssertion]
        assertThat(name == null).isFalse()
        ~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `left operand null check in assertThat detected`() {
        val code =
            """
            package clean

            import kotlin.text.ifEmpty
            import assertk.fail
            import assertk.assertThat
            import assertk.assertions.isEqualTo
            import assertk.assertions.prop

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(null == name).isFalse()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/Testing.kt:12: Warning: Use built-in nullability assertions [NullComparisonAssertion]
        assertThat(null == name).isFalse()
        ~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `not null check in assertThat detected`() {
        val code =
            """
            package clean

            import kotlin.text.ifEmpty
            import assertk.fail
            import assertk.assertThat
            import assertk.assertions.isEqualTo
            import assertk.assertions.prop

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name != null).isFalse()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/Testing.kt:12: Warning: Use built-in nullability assertions [NullComparisonAssertion]
        assertThat(name != null).isFalse()
        ~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `quick fix applied for not null isTrue`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name != null).isTrue()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/Testing.kt line 9: Replace null comparison with null assertion:
@@ -4 +4
+ import assertk.assertions.isNotNull
@@ -9 +10
-         assertThat(name != null).isTrue()
+         assertThat(name).isNotNull()""",
        )
    }

    @Test
    fun `quick fix applied for null isTrue`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name == null).isTrue()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/Testing.kt line 9: Replace null comparison with null assertion:
@@ -4 +4
+ import assertk.assertions.isNull
@@ -9 +10
-         assertThat(name == null).isTrue()
+         assertThat(name).isNull()""",
        )
    }

    @Test
    fun `quick fix applied for not null isFalse`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isFalse

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name != null).isFalse()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/Testing.kt line 9: Replace null comparison with null assertion:
@@ -5 +5
+ import assertk.assertions.isNull
@@ -9 +10
-         assertThat(name != null).isFalse()
+         assertThat(name).isNull()""",
        )
    }

    @Test
    fun `quick fix applied for null isFalse`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isFalse

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name == null).isFalse()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/Testing.kt line 9: Replace null comparison with null assertion:
@@ -5 +5
+ import assertk.assertions.isNotNull
@@ -9 +10
-         assertThat(name == null).isFalse()
+         assertThat(name).isNotNull()""",
        )
    }

    @Test
    fun `clean equality check`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isEqualTo

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name).isEqualTo("Test")
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectClean()
    }

    @Test
    fun `equality in assertThat detected`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isFalse

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name == "Test").isFalse()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/Testing.kt:9: Warning: Use equality assertions [EqualityComparisonAssertion]
        assertThat(name == "Test").isFalse()
        ~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }
}
