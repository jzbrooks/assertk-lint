package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MapAssertionDetectorTest : LintDetectorTest() {
    override fun getDetector() = MapAssertionDetector()

    override fun getIssues() =
        listOf(
            MapAssertionDetector.DIRECT_READ_ISSUE,
            MapAssertionDetector.KEYS_SET_CHECK,
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
    fun `get operator read from map in assertion subject creation detected`() {
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

        lint().files(kotlin(code), kotlin(assertkStub)).run().expect(
            """src/clean/TestingTesting.kt:12: Warning: assertk provides Assert<Map<U, V>>.key(U) to make assertions on particular map values [MapValueAssertion]
        assertThat(map["9A3E6FAC-0639-4F52-8E88-D9F7512540A4"]).isNotNull()
                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `get function read from map in assertion subject creation detected`() {
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

                    assertThat(map.get("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")).isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), kotlin(assertkStub)).run().expect(
            """src/clean/TestingTesting.kt:12: Warning: assertk provides Assert<Map<U, V>>.key(U) to make assertions on particular map values [MapValueAssertion]
        assertThat(map.get("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")).isNotNull()
                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `getValue function read from map in assertion subject creation detected`() {
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

                    assertThat(map.getValue("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")).isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), kotlin(assertkStub)).run().expect(
            """src/clean/TestingTesting.kt:12: Warning: assertk provides Assert<Map<U, V>>.key(U) to make assertions on particular map values [MapValueAssertion]
        assertThat(map.getValue("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")).isNotNull()
                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `getOrDefault function read from map in assertion subject creation detected`() {
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

                    assertThat(map.getOrDefault("9A3E6FAC-0639-4F52-8E88-D9F7512540A4", null)).isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), kotlin(assertkStub)).run().expect(
            """src/clean/TestingTesting.kt:12: Warning: assertk provides Assert<Map<U, V>>.key(U) to make assertions on particular map values [MapValueAssertion]
        assertThat(map.getOrDefault("9A3E6FAC-0639-4F52-8E88-D9F7512540A4", null)).isNotNull()
                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `getOrElse function read from map in assertion subject creation detected`() {
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

                    assertThat(map.getOrElse("9A3E6FAC-0639-4F52-8E88-D9F7512540A4") { error("Nope") }).isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), kotlin(assertkStub)).run().expect(
            """src/clean/TestingTesting.kt:12: Warning: assertk provides Assert<Map<U, V>>.key(U) to make assertions on particular map values [MapValueAssertion]
        assertThat(map.getOrElse("9A3E6FAC-0639-4F52-8E88-D9F7512540A4") { error("Nope") }).isNotNull()
                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `map keys read for checking key not present`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.doesNotContain

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map.keys).doesNotContain("")
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), kotlin(assertkStub), kotlin(assertkCollectionStub)).run().expect(
            """src/clean/TestingTesting.kt:11: Warning: assertk provides Assert<Map<U, V>>.doesNotContainKey() to assert that a key is not present in a map [KeysSetCheck]
        assertThat(map.keys).doesNotContain("")
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `map keys kprop for checking key not present`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.doesNotContain

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map::keys).doesNotContain("")
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), kotlin(assertkStub), kotlin(assertkCollectionStub)).run().expect(
            """src/clean/TestingTesting.kt:11: Warning: assertk provides Assert<Map<U, V>>.doesNotContainKey() to assert that a key is not present in a map [KeysSetCheck]
        assertThat(map::keys).doesNotContain("")
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `map keys read for doesNotContain quick fixed`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.doesNotContain

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map.keys).doesNotContain("")
                }
            }
            """.trimIndent()

        lint().files(
            kotlin(code),
            kotlin(assertkStub),
            kotlin(assertkCollectionStub),
        ).run().expectFixDiffs(
            """Fix for src/clean/TestingTesting.kt line 11: Replace with assertThat(map).doesNotContainKey(""):
            |@@ -3 +3
            |+ import assertk.assertions.doesNotContainKey
            |@@ -11 +12
            |-         assertThat(map.keys).doesNotContain("")
            |+         assertThat(map).doesNotContainKey("")
            """.trimMargin(),
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

            fun <T, U> Assert<Map<T, U>>.key(key: T): Assert<U> {

            }

            fun <T> Assert<T?>.isNotNull(): Assert<T> {

            }
            """.trimIndent()

        val assertkCollectionStub =
            """
            package assertk.assertions

            // This name is a hack to get the test infractructure to correctly
            // name this test stub file's class to AssertkKt
            class Iterable {
            }

            fun <T> Assert<Iterable<T>>.doesNotContain(value: T) {

            }
            """.trimIndent()
    }
}
