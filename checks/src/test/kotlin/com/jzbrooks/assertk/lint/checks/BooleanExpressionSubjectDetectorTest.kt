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
            BooleanExpressionSubjectDetector.BOOLEAN_EQUALITY_ISSUE,
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

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:9: Warning: Use built-in nullability assertions [NullComparisonAssertion]
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

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:12: Warning: Use built-in nullability assertions [NullComparisonAssertion]
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

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:12: Warning: Use built-in nullability assertions [NullComparisonAssertion]
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

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace null comparison with null assertion:
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

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace null comparison with null assertion:
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

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace null comparison with null assertion:
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

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace null comparison with null assertion:
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

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:9: Warning: Use equality assertions [EqualityComparisonAssertion]
        assertThat(name == "Test").isFalse()
        ~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
            )
    }

    @Test
    fun `quick fix applied for not equals isTrue`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name != "Test").isTrue()
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
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace equality check with equality assertion:
@@ -4 +4
+ import assertk.assertions.isNotEqualTo
@@ -9 +10
-         assertThat(name != "Test").isTrue()
+         assertThat(name).isNotEqualTo("Test")""",
            )
    }

    @Test
    fun `quick fix applied for equals isTrue`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name == "Test").isTrue()
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
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace equality check with equality assertion:
@@ -4 +4
+ import assertk.assertions.isEqualTo
@@ -9 +10
-         assertThat(name == "Test").isTrue()
+         assertThat(name).isEqualTo("Test")""",
            )
    }

    @Test
    fun `quick fix applied for not equals isFalse`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isFalse

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name != "Test").isFalse()
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
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace equality check with equality assertion:
@@ -5 +5
+ import assertk.assertions.isEqualTo
@@ -9 +10
-         assertThat(name != "Test").isFalse()
+         assertThat(name).isEqualTo("Test")""",
            )
    }

    @Test
    fun `quick fix applied for equals isFalse`() {
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

        lint()
            .files(
                kotlin(
                    "test/kotlin/test/pkg/UnitTestKotlin.kt",
                    code,
                ),
                *ASSERTK_STUBS,
            ).run()
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace equality check with equality assertion:
@@ -5 +5
+ import assertk.assertions.isNotEqualTo
@@ -9 +10
-         assertThat(name == "Test").isFalse()
+         assertThat(name).isNotEqualTo("Test")""",
            )
    }

    @Test
    fun `quick fix finds literal for assertion argument`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat("Test" == name).isTrue()
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
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace equality check with equality assertion:
@@ -4 +4
+ import assertk.assertions.isEqualTo
@@ -9 +10
-         assertThat("Test" == name).isTrue()
+         assertThat(name).isEqualTo("Test")""",
            )
    }

    @Test
    fun `quick fix treats string template as literal for assertion argument`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat("Test ${'$'}{System.identityHashCode(name)}" == name).isTrue()
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
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace equality check with equality assertion:
@@ -4 +4
+ import assertk.assertions.isEqualTo
@@ -9 +10
-         assertThat("Test ${'$'}{System.identityHashCode(name)}" == name).isTrue()
+         assertThat(name).isEqualTo("Test ${'$'}{System.identityHashCode(name)}")""",
            )
    }

    @Test
    fun `quick fix handles complex binary operator expressions`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(System.identityHashCode(name) == (0..10).random()).isTrue()
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
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace equality check with equality assertion:
@@ -4 +4
+ import assertk.assertions.isEqualTo
@@ -9 +10
-         assertThat(System.identityHashCode(name) == (0..10).random()).isTrue()
+         assertThat(System.identityHashCode(name)).isEqualTo((0..10).random())""",
            )
    }

    @Test
    fun `boolean isEqualTo true fixed to isTrue`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isEqualTo

            class Testing {
                fun test(other: String) {
                    val flag: Boolean = other.isEmpty()
                    assertThat(flag).isEqualTo(true)
                }
            }
            """.trimIndent()

        lint()
            .files(kotlin("test/kotlin/test/pkg/UnitTestKotlin.kt", code), *ASSERTK_STUBS)
            .run()
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace with isTrue():
@@ -4 +4
+ import assertk.assertions.isTrue
@@ -9 +10
-         assertThat(flag).isEqualTo(true)
+         assertThat(flag).isTrue()""",
            )
    }

    @Test
    fun `boolean isEqualTo false fixed to isFalse`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isEqualTo

            class Testing {
                fun test(other: String) {
                    val flag: Boolean = other.isEmpty()
                    assertThat(flag).isEqualTo(false)
                }
            }
            """.trimIndent()

        lint()
            .files(kotlin("test/kotlin/test/pkg/UnitTestKotlin.kt", code), *ASSERTK_STUBS)
            .run()
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace with isFalse():
@@ -4 +4
+ import assertk.assertions.isFalse
@@ -9 +10
-         assertThat(flag).isEqualTo(false)
+         assertThat(flag).isFalse()""",
            )
    }

    @Test
    fun `nullable boolean isEqualTo false fixed to isNotNull isFalse`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isEqualTo

            class Testing {
                fun test(other: String) {
                    val flag: Boolean? = other.isEmpty()
                    assertThat(flag).isEqualTo(false)
                }
            }
            """.trimIndent()

        lint()
            .files(kotlin("test/kotlin/test/pkg/UnitTestKotlin.kt", code), *ASSERTK_STUBS)
            .run()
            .expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace with isNotNull().isFalse():
@@ -4 +4
+ import assertk.assertions.isFalse
+ import assertk.assertions.isNotNull
@@ -9 +11
-         assertThat(flag).isEqualTo(false)
+         assertThat(flag).isNotNull().isFalse()""",
            )
    }

    @Test
    fun `null comparison with isEqualTo false fixed in one step`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isEqualTo

            class Testing {
                fun test(other: String) {
                    val name: String? = other.ifEmpty { null }
                    assertThat(name == null).isEqualTo(false)
                }
            }
            """.trimIndent()

        lint()
            .files(kotlin("test/kotlin/test/pkg/UnitTestKotlin.kt", code), *ASSERTK_STUBS)
            .run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:9: Warning: Use built-in nullability assertions [NullComparisonAssertion]
        assertThat(name == null).isEqualTo(false)
        ~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
            ).expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 9: Replace null comparison with null assertion:
@@ -4 +4
+ import assertk.assertions.isNotNull
@@ -9 +10
-         assertThat(name == null).isEqualTo(false)
+         assertThat(name).isNotNull()""",
            )
    }

    @Test
    fun `equality comparison with isEqualTo true fixed in one step`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isEqualTo

            class Testing {
                fun test(other: String) {
                    assertThat(other == "a").isEqualTo(true)
                }
            }
            """.trimIndent()

        lint()
            .files(kotlin("test/kotlin/test/pkg/UnitTestKotlin.kt", code), *ASSERTK_STUBS)
            .run()
            .expect(
                """test/kotlin/test/pkg/UnitTestKotlin.kt:8: Warning: Use equality assertions [EqualityComparisonAssertion]
        assertThat(other == "a").isEqualTo(true)
        ~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
            ).expectFixDiffs(
                """Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 8: Replace equality check with equality assertion:
@@ -8 +8
-         assertThat(other == "a").isEqualTo(true)
+         assertThat(other).isEqualTo("a")""",
            )
    }

    @Test
    fun `boolean isEqualTo variable is clean`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isEqualTo

            class Testing {
                fun test(other: String) {
                    val expected = true
                    assertThat(other.isEmpty()).isEqualTo(expected)
                }
            }
            """.trimIndent()

        lint()
            .files(kotlin("test/kotlin/test/pkg/UnitTestKotlin.kt", code), *ASSERTK_STUBS)
            .run()
            .expectClean()
    }
}
