package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SingleAssertionDetectorTest : LintDetectorTest() {
    override fun getDetector() = SingleAssertionDetector()

    override fun getIssues() =
        listOf(
            SingleAssertionDetector.ISSUE,
        )

    @Test
    fun `single assertion reports clean`() {
        val code =
            """
            package clean

            import assertk.assertThat
            import assertk.assertions.isEqualTo
            import assertk.assertions.single

            class TestingTesting {
                fun testingTest() {
                    val list: List<String> = listOf("a")

                    assertThat(list).single().isEqualTo("a")
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
    fun `hasSize one and index zero pattern is reported`() {
        val code =
            """
            package clean

            import assertk.all
            import assertk.assertThat
            import assertk.assertions.hasSize
            import assertk.assertions.index
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val list: List<String> = listOf("a")

                    assertThat(list).all {
                        hasSize(1)
                        index(0).isEqualTo("a")
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
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """
                test/kotlin/test/pkg/UnitTestKotlin.kt:13: Warning: Use single assertion for one-element collection assertions [UseSingleAssertion]
                        assertThat(list).all {
                                         ^
                0 errors, 1 warnings
                """.trimIndent(),
            )
    }

    @Test
    fun `hasSize one and first pattern is reported`() {
        val code =
            """
            package clean

            import assertk.all
            import assertk.assertThat
            import assertk.assertions.first
            import assertk.assertions.hasSize
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val list: List<String> = listOf("a")

                    assertThat(list).all {
                        hasSize(1)
                        first().isEqualTo("a")
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
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """
                test/kotlin/test/pkg/UnitTestKotlin.kt:13: Warning: Use single assertion for one-element collection assertions [UseSingleAssertion]
                        assertThat(list).all {
                                         ^
                0 errors, 1 warnings
                """.trimIndent(),
            )
    }

    @Test
    fun `reversed order pattern is reported`() {
        val code =
            """
            package clean

            import assertk.all
            import assertk.assertThat
            import assertk.assertions.hasSize
            import assertk.assertions.index
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val list: List<String> = listOf("a")

                    assertThat(list).all {
                        index(0).isEqualTo("a")
                        hasSize(1)
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
                *ASSERTK_STUBS,
            ).run()
            .expect(
                """
                test/kotlin/test/pkg/UnitTestKotlin.kt:13: Warning: Use single assertion for one-element collection assertions [UseSingleAssertion]
                        assertThat(list).all {
                                         ^
                0 errors, 1 warnings
                """.trimIndent(),
            )
    }

    @Test
    fun `hasSize two reports clean`() {
        val code =
            """
            package clean

            import assertk.all
            import assertk.assertThat
            import assertk.assertions.hasSize
            import assertk.assertions.index
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val list: List<String> = listOf("a", "b")

                    assertThat(list).all {
                        hasSize(2)
                        index(0).isEqualTo("a")
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
                *ASSERTK_STUBS,
            ).run()
            .expectClean()
    }

    @Test
    fun `non-zero index reports clean`() {
        val code =
            """
            package clean

            import assertk.all
            import assertk.assertThat
            import assertk.assertions.hasSize
            import assertk.assertions.index
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val list: List<String> = listOf("a", "b")

                    assertThat(list).all {
                        hasSize(2)
                        index(1).isEqualTo("b")
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
                *ASSERTK_STUBS,
            ).run()
            .expectClean()
    }

    @Test
    fun `more than two statements reports clean`() {
        val code =
            """
            package clean

            import assertk.all
            import assertk.assertThat
            import assertk.assertions.contains
            import assertk.assertions.hasSize
            import assertk.assertions.index
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val list: List<String> = listOf("a")

                    assertThat(list).all {
                        hasSize(1)
                        index(0).isEqualTo("a")
                        contains("a")
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
                *ASSERTK_STUBS,
            ).run()
            .expectClean()
    }

    @Test
    fun `hasSize one and index zero pattern is fixed`() {
        val code =
            """
            package clean

            import assertk.all
            import assertk.assertThat
            import assertk.assertions.hasSize
            import assertk.assertions.index
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val list: List<String> = listOf("a")

                    assertThat(list).all {
                        hasSize(1)
                        index(0).isEqualTo("a")
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
                *ASSERTK_STUBS,
            ).run()
            .expectFixDiffs(
                """
                Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 13: Replace with single().isEqualTo("a"):
                @@ -7,0 +8 @@
                +import assertk.assertions.single
                @@ -13,4 +14 @@
                -        assertThat(list).all {
                -            hasSize(1)
                -            index(0).isEqualTo("a")
                -        }
                +        assertThat(list).single().isEqualTo("a")
                """.trimIndent(),
            )
    }

    @Test
    fun `hasSize one and first pattern is fixed`() {
        val code =
            """
            package clean

            import assertk.all
            import assertk.assertThat
            import assertk.assertions.first
            import assertk.assertions.hasSize
            import assertk.assertions.isEqualTo

            class TestingTesting {
                fun testingTest() {
                    val list: List<String> = listOf("a")

                    assertThat(list).all {
                        hasSize(1)
                        first().isEqualTo("a")
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
                *ASSERTK_STUBS,
            ).run()
            .expectFixDiffs(
                """
                Fix for test/kotlin/test/pkg/UnitTestKotlin.kt line 13: Replace with single().isEqualTo("a"):
                @@ -7,0 +8 @@
                +import assertk.assertions.single
                @@ -13,4 +14 @@
                -        assertThat(list).all {
                -            hasSize(1)
                -            first().isEqualTo("a")
                -        }
                +        assertThat(list).single().isEqualTo("a")
                """.trimIndent(),
            )
    }
}
