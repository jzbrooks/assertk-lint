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
            MapAssertionDetector.KEYS_SET_PRESENT_ISSUE,
            MapAssertionDetector.KEYS_SET_ABSENT_ISSUE,
        )

    @Test
    fun `no issues reports clean`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.key
            import assertk.assertions.isNotNull

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map).key("9A3E6FAC-0639-4F52-8E88-D9F7512540A4").isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectClean()
    }

    @Test
    fun `get operator read from map in assertion subject creation detected`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.key
            import assertk.assertions.isNotNull

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map["9A3E6FAC-0639-4F52-8E88-D9F7512540A4"]).isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/TestingTesting.kt:12: Warning: Use Assert.key for map entries [MapValueAssertion]
        assertThat(map["9A3E6FAC-0639-4F52-8E88-D9F7512540A4"]).isNotNull()
                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `get operator read from map replaced with key`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.isNotNull

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map["9A3E6FAC-0639-4F52-8E88-D9F7512540A4"]).isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """
            Fix for src/clean/TestingTesting.kt line 11: Replace with assertThat(map).key("9A3E6FAC-0639-4F52-8E88-D9F7512540A4"):
            @@ -3 +3
            + import assertk.assertions.key
            @@ -11 +12
            -         assertThat(map["9A3E6FAC-0639-4F52-8E88-D9F7512540A4"]).isNotNull()
            +         assertThat(map).key("9A3E6FAC-0639-4F52-8E88-D9F7512540A4").isNotNull()
            """.trimIndent(),
        )
    }

    @Test
    fun `get function read from map in assertion subject creation detected`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.key
            import assertk.assertions.isNotNull

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map.get("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")).isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/TestingTesting.kt:12: Warning: Use Assert.key for map entries [MapValueAssertion]
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
            import assertk.assertions.key
            import assertk.assertions.isNotNull

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map.getValue("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")).isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/TestingTesting.kt:12: Warning: Use Assert.key for map entries [MapValueAssertion]
        assertThat(map.getValue("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")).isNotNull()
                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `getValue function read from map replaced with key`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.isNotNull

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map.getValue("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")).isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/TestingTesting.kt line 11: Replace with assertThat(map).key("9A3E6FAC-0639-4F52-8E88-D9F7512540A4"):
@@ -3 +3
+ import assertk.assertions.key
@@ -11 +12
-         assertThat(map.getValue("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")).isNotNull()
+         assertThat(map).key("9A3E6FAC-0639-4F52-8E88-D9F7512540A4").isNotNull()""",
        )
    }

    @Test
    fun `getOrDefault function read from map in assertion subject creation detected`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.key
            import assertk.assertions.isNotNull

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map.getOrDefault("9A3E6FAC-0639-4F52-8E88-D9F7512540A4", null)).isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/TestingTesting.kt:12: Warning: Use Assert.key for map entries [MapValueAssertion]
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
            import assertk.assertions.key
            import assertk.assertions.isNotNull

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map.getOrElse("9A3E6FAC-0639-4F52-8E88-D9F7512540A4") { error("Nope") }).isNotNull()
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/TestingTesting.kt:12: Warning: Use Assert.key for map entries [MapValueAssertion]
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

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/TestingTesting.kt:11: Warning: Use Assert.doesNotContainKey to assert absence [KeySetAbsentAssertion]
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

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/TestingTesting.kt:11: Warning: Use Assert.doesNotContainKey to assert absence [KeySetAbsentAssertion]
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

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/TestingTesting.kt line 11: Replace with assertThat(map).doesNotContainKey(""):
            |@@ -3 +3
            |+ import assertk.assertions.doesNotContainKey
            |@@ -11 +12
            |-         assertThat(map.keys).doesNotContain("")
            |+         assertThat(map).doesNotContainKey("")
            """.trimMargin(),
        )
    }

    @Test
    fun `map keys read for checking key is present`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.contains

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map.keys).contains("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expect(
            """src/clean/TestingTesting.kt:11: Warning: Use Assert.key to assert presence [KeySetPresentAssertion]
        assertThat(map.keys).contains("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    @Test
    fun `map keys read for contains quick fixed`() {
        val code =
            """
            package clean

            import java.io.File
            import assertk.assertThat
            import assertk.assertions.contains

            class TestingTesting {
                fun testingTest() {
                    val map: Map<String, String?> = mapOf("9A3E6FAC-0639-4F52-8E88-D9F7512540A4" to "John")

                    assertThat(map.keys).contains("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")
                }
            }
            """.trimIndent()

        lint().files(kotlin(code), *ASSERTK_STUBS).run().expectFixDiffs(
            """Fix for src/clean/TestingTesting.kt line 11: Replace with assertThat(map).key("9A3E6FAC-0639-4F52-8E88-D9F7512540A4"):
            |@@ -3 +3
            |+ import assertk.assertions.key
            |@@ -11 +12
            |-         assertThat(map.keys).contains("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")
            |+         assertThat(map).key("9A3E6FAC-0639-4F52-8E88-D9F7512540A4")
            """.trimMargin(),
        )
    }
}
