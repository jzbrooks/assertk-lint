package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TestFrameworkAssertionDetectorTest : LintDetectorTest() {
    override fun getDetector() = TestFrameworkAssertionDetector()

    override fun getIssues() =
        listOf(
            TestFrameworkAssertionDetector.ISSUE,
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

        lint().files(kotlin(code)).run().expectClean()
    }

    @Test
    fun `junit 4 assertion not reported for java source`() {
        val code =
            """
            package error;

            import java.io.File;
            import org.junit.Assert.assertEquals;

            class TestingTesting {
                fun testingTest() {
                    val first = File();
                    val second = File();
                    assertEquals(first, second);
                }
            }
            """.trimIndent()

        lint()
            .files(
                java(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectClean()
    }

    @Test
    fun `junit 4 assertion detected`() {
        val code =
            """
            package error

            import java.io.File
            import org.junit.Assert.assertEquals

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()
                    assertEquals(first, second)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expect(
                """src/error/TestingTesting.kt:10: Warning: Use assertk assertions [TestFrameworkAssertionUse]
        assertEquals(first, second)
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
            )
    }

    @Test
    fun `junit 4 assertEquals fixed`() {
        val code =
            """
            package error

            import java.io.File
            import org.junit.Assert.assertEquals

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()
                    assertEquals(first, second)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectFixDiffs(
                """Fix for src/error/TestingTesting.kt line 10: Replace with assertThat(second).isEqualTo(first):
@@ -3 +3
+ import assertk.assertThat
+ import assertk.assertions.isEqualTo
@@ -10 +12
-         assertEquals(first, second)
+         assertThat(second).isEqualTo(first)""",
            )
    }

    @Test
    fun `junit 4 assertNotEquals fixed`() {
        val code =
            """
            package error

            import java.io.File
            import org.junit.Assert.assertNotEquals

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()
                    assertNotEquals(first, second)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectFixDiffs(
                """Fix for src/error/TestingTesting.kt line 10: Replace with assertThat(second).isNotEqualTo(first):
@@ -3 +3
+ import assertk.assertThat
+ import assertk.assertions.isNotEqualTo
@@ -10 +12
-         assertNotEquals(first, second)
+         assertThat(second).isNotEqualTo(first)""",
            )
    }

    @Test
    fun `junit 4 assertEquals with message fixed`() {
        val code =
            """
            package error

            import java.io.File
            import org.junit.Assert.assertEquals

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()
                    assertEquals("The files are in the computer", first, second)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectFixDiffs(
                """Fix for src/error/TestingTesting.kt line 10: Replace with assertThat(second).isEqualTo(first):
@@ -3 +3
+ import assertk.assertThat
+ import assertk.assertions.isEqualTo
@@ -10 +12
-         assertEquals("The files are in the computer", first, second)
+         assertThat(second).isEqualTo(first)""",
            )
    }

    @Test
    fun `junit 4 assertTrue fixed`() {
        val code =
            """
            package error

            import java.io.File
            import org.junit.Assert.assertTrue

            class TestingTesting {
                fun testingTest() {
                    val condition = true
                    assertTrue(condition)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectFixDiffs(
                """Fix for src/error/TestingTesting.kt line 9: Replace with assertThat(condition).isTrue():
@@ -3 +3
+ import assertk.assertThat
+ import assertk.assertions.isTrue
@@ -9 +11
-         assertTrue(condition)
+         assertThat(condition).isTrue()""",
            )
    }

    @Test
    fun `junit 4 assertTrue with message fixed`() {
        val code =
            """
            package error

            import java.io.File
            import org.junit.Assert.assertTrue

            class TestingTesting {
                fun testingTest() {
                    val condition = true
                    assertTrue("Should be true", condition)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectFixDiffs(
                """Fix for src/error/TestingTesting.kt line 9: Replace with assertThat(condition).isTrue():
@@ -3 +3
+ import assertk.assertThat
+ import assertk.assertions.isTrue
@@ -9 +11
-         assertTrue("Should be true", condition)
+         assertThat(condition).isTrue()""",
            )
    }

    @Test
    fun `junit 4 assertFalse fixed`() {
        val code =
            """
            package error

            import java.io.File
            import org.junit.Assert.assertFalse

            class TestingTesting {
                fun testingTest() {
                    val condition = false
                    assertFalse(condition)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectFixDiffs(
                """Fix for src/error/TestingTesting.kt line 9: Replace with assertThat(condition).isFalse():
@@ -3 +3
+ import assertk.assertThat
+ import assertk.assertions.isFalse
@@ -9 +11
-         assertFalse(condition)
+         assertThat(condition).isFalse()""",
            )
    }

    @Test
    fun `junit 4 assertNull fixed`() {
        val code =
            """
            package error

            import org.junit.Assert.assertNull

            class TestingTesting {
                fun testingTest() {
                    val value: String? = null
                    assertNull(value)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectFixDiffs(
                """Fix for src/error/TestingTesting.kt line 8: Replace with assertThat(value).isNull():
@@ -3 +3
+ import assertk.assertThat
+ import assertk.assertions.isNull
@@ -8 +10
-         assertNull(value)
+         assertThat(value).isNull()""",
            )
    }

    @Test
    fun `junit 4 assertNotNull fixed`() {
        val code =
            """
            package error

            import org.junit.Assert.assertNotNull

            class TestingTesting {
                fun testingTest() {
                    val value = "not null"
                    assertNotNull(value)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectFixDiffs(
                """Fix for src/error/TestingTesting.kt line 8: Replace with assertThat(value).isNotNull():
@@ -3 +3
+ import assertk.assertThat
+ import assertk.assertions.isNotNull
@@ -8 +10
-         assertNotNull(value)
+         assertThat(value).isNotNull()""",
            )
    }

    @Test
    fun `junit 4 assertSame fixed`() {
        val code =
            """
            package error

            import java.io.File
            import org.junit.Assert.assertSame

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = first
                    assertSame(first, second)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectFixDiffs(
                """Fix for src/error/TestingTesting.kt line 10: Replace with assertThat(second).isSameAs(first):
@@ -3 +3
+ import assertk.assertThat
+ import assertk.assertions.isSameAs
@@ -10 +12
-         assertSame(first, second)
+         assertThat(second).isSameAs(first)""",
            )
    }

    @Test
    fun `junit 4 assertNotSame fixed`() {
        val code =
            """
            package error

            import java.io.File
            import org.junit.Assert.assertNotSame

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()
                    assertNotSame(first, second)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectFixDiffs(
                """Fix for src/error/TestingTesting.kt line 10: Replace with assertThat(second).isNotSameAs(first):
@@ -3 +3
+ import assertk.assertThat
+ import assertk.assertions.isNotSameAs
@@ -10 +12
-         assertNotSame(first, second)
+         assertThat(second).isNotSameAs(first)""",
            )
    }

    @Test
    fun `junit 4 assertArrayEquals fixed`() {
        val code =
            """
            package error

            import org.junit.Assert.assertArrayEquals

            class TestingTesting {
                fun testingTest() {
                    val expected = intArrayOf(1, 2, 3)
                    val actual = intArrayOf(1, 2, 3)
                    assertArrayEquals(expected, actual)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectFixDiffs(
                """Fix for src/error/TestingTesting.kt line 9: Replace with assertThat(actual).containsOnly(*expected):
@@ -3 +3
+ import assertk.assertThat
+ import assertk.assertions.containsOnly
@@ -9 +11
-         assertArrayEquals(expected, actual)
+         assertThat(actual).containsOnly(*expected)""",
            )
    }

    @Test
    fun `junit 5 assertion detected`() {
        val code =
            """
            package error

            import java.io.File
            import org.junit.jupiter.api.Assertions.assertEquals

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()
                    assertEquals(first, second)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_5_ASSERT_STUB),
            ).run()
            .expect(
                """src/error/TestingTesting.kt:10: Warning: Use assertk assertions [TestFrameworkAssertionUse]
        assertEquals(first, second)
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
            )
    }

    @Test
    fun `kotlin assertion detected`() {
        val code =
            """
            package error

            import java.io.File
            import kotlin.test.assertEquals

            class TestingTesting {
                fun testingTest() {
                    val first = File()
                    val second = File()
                    assertEquals(first, second)
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                kotlin(KOTLIN_TEST_ASSERT_STUB),
            ).run()
            .expect(
                """src/error/TestingTesting.kt:10: Warning: Use assertk assertions [TestFrameworkAssertionUse]
        assertEquals(first, second)
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
            )
    }

    @Test
    fun `junit 4 fail fixed`() {
        val code =
            """
            package error

            import org.junit.Assert.fail

            class TestingTesting {
                fun testingTest() {
                    fail("This test should fail")
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectFixDiffs(
                """Fix for src/error/TestingTesting.kt line 7: Replace with assertk.fail("This test should fail"):
@@ -7 +7
-         fail("This test should fail")
+         assertk.fail("This test should fail")""",
            )
    }

    @Test
    fun `junit 4 fail without message fixed`() {
        val code =
            """
            package error

            import org.junit.Assert.fail

            class TestingTesting {
                fun testingTest() {
                    fail()
                }
            }
            """.trimIndent()

        lint()
            .files(
                kotlin(code),
                java(JUNIT_4_ASSERT_STUB),
            ).run()
            .expectFixDiffs(
                """Fix for src/error/TestingTesting.kt line 7: Replace with assertk.fail():
@@ -7 +7
-         fail()
+         assertk.fail()""",
            )
    }

    companion object {
        private const val JUNIT_4_ASSERT_STUB = """
            package org.junit;

            public class Assert {
                public static void assertEquals(Object expected, Object actual) {
                }

                public static void assertEquals(String message, Object expected, Object actual) {
                }

                public static void assertNotEquals(Object unexpected, Object actual) {
                }

                public static void assertNotEquals(String message, Object unexpected, Object actual) {
                }

                public static void assertTrue(boolean condition) {
                }

                public static void assertTrue(String message, boolean condition) {
                }

                public static void assertFalse(boolean condition) {
                }

                public static void assertFalse(String message, boolean condition) {
                }

                public static void assertNull(Object actual) {
                }

                public static void assertNull(String message, Object actual) {
                }

                public static void assertNotNull(Object actual) {
                }

                public static void assertNotNull(String message, Object actual) {
                }

                public static void assertSame(Object expected, Object actual) {
                }

                public static void assertSame(String message, Object expected, Object actual) {
                }

                public static void assertNotSame(Object unexpected, Object actual) {
                }

                public static void assertNotSame(String message, Object unexpected, Object actual) {
                }

                public static void assertArrayEquals(Object[] expected, Object[] actual) {
                }

                public static void assertArrayEquals(String message, Object[] expected, Object[] actual) {
                }

                public static void fail() {
                }

                public static void fail(String message) {
                }
            }
        """

        private const val JUNIT_5_ASSERT_STUB = """
            package org.junit.jupiter.api;

            public class Assertions {
                public static void assertEquals(Object expected, Object actual) {
                }
            }
        """

        private const val KOTLIN_TEST_ASSERT_STUB = """
            @file:kotlin.jvm.JvmName("AssertionsKt__AssertionsKt")

            package kotlin.test

            fun <T> assertEquals(expected: T, actual: T) {
            }
        """
    }
}
