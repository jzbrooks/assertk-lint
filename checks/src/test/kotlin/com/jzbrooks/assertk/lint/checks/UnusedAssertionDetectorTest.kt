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

    @Test
    fun `quick fix applied for null check with rhs literal`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name == null)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/Testing.kt line 9: Replace with assertThat(name).isNull():
@@ -4 +4
+ import assertk.assertions.isNull
@@ -9 +10
-         assertThat(name == null)
+         assertThat(name).isNull()""",
        )
    }

    @Test
    fun `quick fix applied for not-null check with rhs literal`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name != null)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/Testing.kt line 9: Replace with assertThat(name).isNotNull():
@@ -4 +4
+ import assertk.assertions.isNotNull
@@ -9 +10
-         assertThat(name != null)
+         assertThat(name).isNotNull()""",
        )
    }

    @Test
    fun `quick fix applied for null check with lhs literal`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(null == name)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/Testing.kt line 9: Replace with assertThat(name).isNull():
@@ -4 +4
+ import assertk.assertions.isNull
@@ -9 +10
-         assertThat(null == name)
+         assertThat(name).isNull()""",
        )
    }

    @Test
    fun `quick fix applied for not-null check with lhs literal`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(null != name)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/Testing.kt line 9: Replace with assertThat(name).isNotNull():
@@ -4 +4
+ import assertk.assertions.isNotNull
@@ -9 +10
-         assertThat(null != name)
+         assertThat(name).isNotNull()""",
        )
    }

    @Test
    fun `quick fix applied for literal non-equality with lhs literal`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat("Test" != name)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/Testing.kt line 9: Replace with assertThat(name).isNotEqualTo("Test"):
@@ -4 +4
+ import assertk.assertions.isNotEqualTo
@@ -9 +10
-         assertThat("Test" != name)
+         assertThat(name).isNotEqualTo("Test")""",
        )
    }

    @Test
    fun `quick fix applied for literal non-equality with rhs literal`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name != "Test")
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/Testing.kt line 9: Replace with assertThat(name).isNotEqualTo("Test"):
@@ -4 +4
+ import assertk.assertions.isNotEqualTo
@@ -9 +10
-         assertThat(name != "Test")
+         assertThat(name).isNotEqualTo("Test")""",
        )
    }

    @Test
    fun `quick fix applied for literal equality with lhs literal`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat("Test" == name)
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/Testing.kt line 9: Replace with assertThat(name).isEqualTo("Test"):
@@ -4 +4
+ import assertk.assertions.isEqualTo
@@ -9 +10
-         assertThat("Test" == name)
+         assertThat(name).isEqualTo("Test")""",
        )
    }

    @Test
    fun `quick fix applied for literal equality with rhs literal`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isTrue

            class Testing {
                fun nameExists() {
                    val name: String? = this::class.simpleName
                    assertThat(name == "Test")
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/Testing.kt line 9: Replace with assertThat(name).isEqualTo("Test"):
@@ -4 +4
+ import assertk.assertions.isEqualTo
@@ -9 +10
-         assertThat(name == "Test")
+         assertThat(name).isEqualTo("Test")""",
        )
    }
}
