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
                        )
                    }
                }
            }

            private fun buildJunit4QuickFix(node: UCallExpression): LintFix? {
                return when (node.methodIdentifier?.name) {
                    "assertEquals" -> {
                        val expectedIndex = if (node.valueArguments.size == 2) 0 else 1
                        val expectedExpr =
                            node.valueArguments.getOrNull(expectedIndex) ?: return null
                        val actualExpr =
                            node.valueArguments.getOrNull(expectedIndex + 1) ?: return null

                        fix()
                            .replace()
                            .range(
                                context.getCallLocation(
                                    node,
                                    includeReceiver = false,
                                    includeArguments = true,
                                ),
                            ).imports("assertk.assertThat", "assertk.assertions.isEqualTo")
                            .with(
                                buildString {
                                    append("assertThat(")
                                    append(actualExpr.sourcePsi!!.text)
                                    append(").isEqualTo(")
                                    append(expectedExpr.sourcePsi!!.text)
                                    append(")")
                                },
                            ).build()
                    }

                    "assertNotEquals" -> {
                        val expectedIndex = if (node.valueArguments.size == 2) 0 else 1
                        val expectedExpr =
                            node.valueArguments.getOrNull(expectedIndex) ?: return null
                        val actualExpr =
                            node.valueArguments.getOrNull(expectedIndex + 1) ?: return null

                        fix()
                            .replace()
                            .range(
                                context.getCallLocation(
                                    node,
                                    includeReceiver = false,
                                    includeArguments = true,
                                ),
                            ).imports("assertk.assertThat", "assertk.assertions.isNotEqualTo")
                            .with(
                                buildString {
                                    append("assertThat(")
                                    append(actualExpr.sourcePsi!!.text)
                                    append(").isNotEqualTo(")
                                    append(expectedExpr.sourcePsi!!.text)
                                    append(")")
                                },
                            ).build()
                    }

                    "assertTrue" -> {
                        val actualIndex = if (node.valueArguments.size == 1) 0 else 1
                        val actualExpr = node.valueArguments.getOrNull(actualIndex) ?: return null

                        fix()
                            .replace()
                            .range(
                                context.getCallLocation(
                                    node,
                                    includeReceiver = false,
                                    includeArguments = true,
                                ),
                            ).imports("assertk.assertThat", "assertk.assertions.isTrue")
                            .with(
                                buildString {
                                    append("assertThat(")
                                    append(actualExpr.sourcePsi!!.text)
                                    append(").isTrue()")
                                },
                            ).build()
                    }

                    "assertFalse" -> {
                        val actualIndex = if (node.valueArguments.size == 1) 0 else 1
                        val actualExpr = node.valueArguments.getOrNull(actualIndex) ?: return null

                        fix()
                            .replace()
                            .range(
                                context.getCallLocation(
                                    node,
                                    includeReceiver = false,
                                    includeArguments = true,
                                ),
                            ).imports("assertk.assertThat", "assertk.assertions.isFalse")
                            .with(
                                buildString {
                                    append("assertThat(")
                                    append(actualExpr.sourcePsi!!.text)
                                    append(").isFalse()")
                                },
                            ).build()
                    }

                    "assertNull" -> {
                        val actualIndex = if (node.valueArguments.size == 1) 0 else 1
                        val actualExpr = node.valueArguments.getOrNull(actualIndex) ?: return null

                        fix()
                            .replace()
                            .range(
                                context.getCallLocation(
                                    node,
                                    includeReceiver = false,
                                    includeArguments = true,
                                ),
                            ).imports("assertk.assertThat", "assertk.assertions.isNull")
                            .with(
                                buildString {
                                    append("assertThat(")
                                    append(actualExpr.sourcePsi!!.text)
                                    append(").isNull()")
                                },
                            ).build()
                    }

                    "assertNotNull" -> {
                        val actualIndex = if (node.valueArguments.size == 1) 0 else 1
                        val actualExpr = node.valueArguments.getOrNull(actualIndex) ?: return null

                        fix()
                            .replace()
                            .range(
                                context.getCallLocation(
                                    node,
                                    includeReceiver = false,
                                    includeArguments = true,
                                ),
                            ).imports("assertk.assertThat", "assertk.assertions.isNotNull")
                            .with(
                                buildString {
                                    append("assertThat(")
                                    append(actualExpr.sourcePsi!!.text)
                                    append(").isNotNull()")
                                },
                            ).build()
                    }

                    "assertSame" -> {
                        val expectedIndex = if (node.valueArguments.size == 2) 0 else 1
                        val expectedExpr =
                            node.valueArguments.getOrNull(expectedIndex) ?: return null
                        val actualExpr =
                            node.valueArguments.getOrNull(expectedIndex + 1) ?: return null

                        fix()
                            .replace()
                            .range(
                                context.getCallLocation(
                                    node,
                                    includeReceiver = false,
                                    includeArguments = true,
                                ),
                            ).imports("assertk.assertThat", "assertk.assertions.isSameAs")
                            .with(
                                buildString {
                                    append("assertThat(")
                                    append(actualExpr.sourcePsi!!.text)
                                    append(").isSameAs(")
                                    append(expectedExpr.sourcePsi!!.text)
                                    append(")")
                                },
                            ).build()
                    }

                    "assertNotSame" -> {
                        val expectedIndex = if (node.valueArguments.size == 2) 0 else 1
                        val expectedExpr =
                            node.valueArguments.getOrNull(expectedIndex) ?: return null
                        val actualExpr =
                            node.valueArguments.getOrNull(expectedIndex + 1) ?: return null

                        fix()
                            .replace()
                            .range(
                                context.getCallLocation(
                                    node,
                                    includeReceiver = false,
                                    includeArguments = true,
                                ),
                            ).imports("assertk.assertThat", "assertk.assertions.isNotSameAs")
                            .with(
                                buildString {
                                    append("assertThat(")
                                    append(actualExpr.sourcePsi!!.text)
                                    append(").isNotSameAs(")
                                    append(expectedExpr.sourcePsi!!.text)
                                    append(")")
                                },
                            ).build()
                    }

                    else -> null
                }
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
