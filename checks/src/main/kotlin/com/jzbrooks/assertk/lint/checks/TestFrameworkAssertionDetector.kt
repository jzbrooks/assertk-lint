package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import java.util.EnumSet

class TestFrameworkAssertionDetector :
    Detector(),
    Detector.UastScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(
            UCallExpression::class.java,
        )

    override fun createUastHandler(context: JavaContext) =
        object : UElementHandler() {
            private val junit4 = "org.junit.Assert"
            private val junit5 = "org.junit.jupiter.api.Assertions"
            private val kotlinTest = "kotlin.test.AssertionsKt__AssertionsKt"

            override fun visitCallExpression(node: UCallExpression) {
                if (!node.isKotlin) return

                val psiMethod = node.resolve()

                when {
                    context.evaluator.isMemberInClass(psiMethod, junit4) -> {
                        context.report(
                            ISSUE,
                            node,
                            context.getLocation(node),
                            "Use assertk assertions",
                            buildJunit4QuickFix(node),
                        )
                    }

                    context.evaluator.isMemberInClass(psiMethod, junit5) -> {
                        context.report(
                            ISSUE,
                            node,
                            context.getLocation(node),
                            "Use assertk assertions",
                        )
                    }

                    context.evaluator.isMemberInClass(psiMethod, kotlinTest) -> {
                        context.report(
                            ISSUE,
                            node,
                            context.getLocation(node),
                            "Use assertk assertions",
                            buildKotlinTestQuickFix(node),
                        )
                    }
                }
            }

            private fun buildJunit4QuickFix(node: UCallExpression): LintFix? =
                when (node.methodIdentifier?.name) {
                    "assertEquals" -> replaceJunit4AssertionWithExpected(node, "isEqualTo")

                    "assertNotEquals" -> replaceJunit4AssertionWithExpected(node, "isNotEqualTo")

                    "assertTrue" -> replaceJunit4AssertionWithoutExpected(node, "isTrue")

                    "assertFalse" -> replaceJunit4AssertionWithoutExpected(node, "isFalse")

                    "assertNull" -> replaceJunit4AssertionWithoutExpected(node, "isNull")

                    "assertNotNull" -> replaceJunit4AssertionWithoutExpected(node, "isNotNull")

                    "assertSame" -> replaceJunit4AssertionWithExpected(node, "isSameAs")

                    "assertNotSame" -> replaceJunit4AssertionWithExpected(node, "isNotSameAs")

                    "assertArrayEquals" ->
                        replaceJunit4AssertionWithExpected(node, "containsOnly") {
                            append("*")
                            append(it.sourcePsi!!.text)
                        }

                    "fail" -> {
                        val messageExpr = node.valueArguments.firstOrNull()

                        fix()
                            .replace()
                            .reformat(true)
                            .range(
                                context.getCallLocation(
                                    node,
                                    includeReceiver = false,
                                    includeArguments = true,
                                ),
                            ).with(
                                buildString {
                                    append("assertk.fail(")
                                    if (messageExpr != null) {
                                        append(messageExpr.sourcePsi!!.text)
                                    }
                                    append(")")
                                },
                            ).build()
                    }

                    else -> null
                }

            private fun replaceJunit4AssertionWithExpected(
                call: UCallExpression,
                assertionFunctionName: String,
                expectedTransformation: StringBuilder.(
                    UExpression,
                ) -> Unit = { append(it.sourcePsi!!.text) },
            ): LintFix? {
                val expectedIndex = if (call.valueArguments.size == 2) 0 else 1
                val expectedExpr =
                    call.valueArguments.getOrNull(expectedIndex) ?: return null
                val actualExpr =
                    call.valueArguments.getOrNull(expectedIndex + 1) ?: return null

                return fix()
                    .replace()
                    .reformat(true)
                    .range(
                        context.getCallLocation(
                            call,
                            includeReceiver = false,
                            includeArguments = true,
                        ),
                    ).imports("assertk.assertThat", "assertk.assertions.$assertionFunctionName")
                    .with(
                        buildString {
                            append("assertThat(")
                            append(actualExpr.sourcePsi!!.text)
                            append(").")
                            append(assertionFunctionName)
                            append("(")
                            expectedTransformation(expectedExpr)
                            append(")")

                            if (call.valueArguments.size == 3) {
                                append(" // ")
                                append(
                                    call.valueArguments
                                        .first()
                                        .sourcePsi!!
                                        .text,
                                )
                            }
                        },
                    ).build()
            }

            private fun replaceJunit4AssertionWithoutExpected(
                call: UCallExpression,
                assertionFunctionName: String,
            ): LintFix? {
                val actualIndex = if (call.valueArguments.size == 1) 0 else 1
                val actualExpr =
                    call.valueArguments.getOrNull(actualIndex) ?: return null

                return fix()
                    .replace()
                    .reformat(true)
                    .range(
                        context.getCallLocation(
                            call,
                            includeReceiver = false,
                            includeArguments = true,
                        ),
                    ).imports("assertk.assertThat", "assertk.assertions.$assertionFunctionName")
                    .with(
                        buildString {
                            append("assertThat(")
                            append(actualExpr.sourcePsi!!.text)
                            append(").")
                            append(assertionFunctionName)
                            append("()")

                            if (call.valueArguments.size == 2) {
                                append(" // ")
                                append(
                                    call.valueArguments
                                        .first()
                                        .sourcePsi!!
                                        .text,
                                )
                            }
                        },
                    ).build()
            }

            private fun buildKotlinTestQuickFix(node: UCallExpression): LintFix? =
                when (node.methodIdentifier?.name) {
                    "assertEquals" -> null
                    "assertNotEquals" -> null
                    "assertNull" -> null
                    "assertNotNull" -> null
                    "assertTrue" -> null
                    "assertFalse" -> null
                    "assertSame" -> null
                    "assertNotSame" -> null
                    "assertIs" -> null
                    "assertNotIs" -> null
                    "assertFails" -> null
                    "fail" -> null
                    else -> null
                }

            private fun replaceKotlinTestAssertionWithExpected(
                call: UCallExpression,
                assertionFunctionName: String,
                expectedTransformation: StringBuilder.(
                    UExpression,
                ) -> Unit = { append(it.sourcePsi!!.text) },
            ): LintFix? {
                val expectedIndex = if (call.valueArguments.size == 2) 0 else 1
                val expectedExpr =
                    call.valueArguments.getOrNull(expectedIndex) ?: return null
                val actualExpr =
                    call.valueArguments.getOrNull(expectedIndex + 1) ?: return null

                return fix()
                    .replace()
                    .reformat(true)
                    .range(
                        context.getCallLocation(
                            call,
                            includeReceiver = false,
                            includeArguments = true,
                        ),
                    ).imports("assertk.assertThat", "assertk.assertions.$assertionFunctionName")
                    .with(
                        buildString {
                            append("assertThat(")
                            append(actualExpr.sourcePsi!!.text)
                            append(").")
                            append(assertionFunctionName)
                            append("(")
                            expectedTransformation(expectedExpr)
                            append(")")

                            if (call.valueArguments.size == 3) {
                                append(" // ")
                                append(
                                    call.valueArguments
                                        .first()
                                        .sourcePsi!!
                                        .text,
                                )
                            }
                        },
                    ).build()
            }
        }

    companion object {
        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "TestFrameworkAssertionUse",
                briefDescription = "Test framework assertion is called",
                explanation = """
                    Test frameworks like junit and kotlin test ship with built-in test assertions. However, these assertion mechanisms shouldn't be used if fluent assertion libraries are on the classpath.
                    """,
                category = Category.CORRECTNESS,
                priority = 6,
                severity = Severity.WARNING,
                implementation =
                    Implementation(
                        TestFrameworkAssertionDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )
    }
}
