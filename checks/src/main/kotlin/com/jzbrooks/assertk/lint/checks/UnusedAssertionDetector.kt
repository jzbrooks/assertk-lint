package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.DataFlowAnalyzer
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.TextFormat
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.skipParenthesizedExprDown
import java.util.EnumSet

class UnusedAssertionDetector :
    Detector(),
    SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> = listOf("assertThat")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        val containingClass = method.containingClass
        val evaluator = context.evaluator

        if (evaluator.extendsClass(containingClass, "assertk.AssertKt", false)) {
            var usedAsReceiver = false
            val visitor =
                object : DataFlowAnalyzer(setOf(node)) {
                    override fun receiver(call: UCallExpression) {
                        usedAsReceiver = true
                    }
                }

            val uElement =
                node.getParentOfType(true, UMethod::class.java, ULambdaExpression::class.java)
            uElement?.accept(visitor)

            if (!usedAsReceiver) {
                context.report(
                    ISSUE,
                    node,
                    context.getCallLocation(
                        call = node,
                        includeReceiver = true,
                        includeArguments = true,
                    ),
                    ISSUE.getBriefDescription(TextFormat.TEXT),
                    buildFix(context, node),
                )
            }
        }
    }

    /**
     * TODO: There's a good deal of duplication w.r.t. the quick fixes
     * in [BooleanExpressionSubjectDetector]. An abstraction might be in order soon.
     */
    private fun buildFix(
        context: JavaContext,
        assertThat: UCallExpression,
    ): LintFix? {
        val argExpr =
            assertThat.valueArguments
                .map { it.skipParenthesizedExprDown() }
                .firstOrNull() ?: return null

        val psi = argExpr.sourcePsi
        return when {
            argExpr is UBinaryExpression ->
                buildFixForBinaryExpression(
                    context,
                    assertThat,
                    argExpr,
                )

            psi is KtIsExpression -> buildFixForTypeCheck(context, assertThat, psi)

            else -> null
        }
    }

    private fun buildFixForBinaryExpression(
        context: JavaContext,
        assertThat: UCallExpression,
        binaryExpr: UBinaryExpression,
    ): LintFix? {
        return when {
            binaryExpr.isNullComparisonExpr -> {
                when {
                    (binaryExpr.rightOperand as? ULiteralExpression)?.isNull == true -> {
                        when (binaryExpr.operator) {
                            UastBinaryOperator.EQUALS -> {
                                fix()
                                    .replace()
                                    .range(
                                        context.getCallLocation(
                                            assertThat,
                                            includeReceiver = false,
                                            includeArguments = true,
                                        ),
                                    ).imports("assertk.assertions.isNull")
                                    .with(
                                        buildString {
                                            append("assertThat(")
                                            append(binaryExpr.leftOperand.sourcePsi!!.text)
                                            append(").isNull()")
                                        },
                                    ).reformat(true)
                                    .build()
                            }

                            UastBinaryOperator.NOT_EQUALS ->
                                fix()
                                    .replace()
                                    .range(
                                        context.getCallLocation(
                                            assertThat,
                                            includeReceiver = false,
                                            includeArguments = true,
                                        ),
                                    ).imports("assertk.assertions.isNotNull")
                                    .with(
                                        buildString {
                                            append("assertThat(")
                                            append(binaryExpr.leftOperand.sourcePsi!!.text)
                                            append(").isNotNull()")
                                        },
                                    ).reformat(true)
                                    .build()

                            else -> null
                        }
                    }

                    (binaryExpr.leftOperand as? ULiteralExpression)?.isNull == true -> {
                        when (binaryExpr.operator) {
                            UastBinaryOperator.EQUALS -> {
                                fix()
                                    .replace()
                                    .range(
                                        context.getCallLocation(
                                            assertThat,
                                            includeReceiver = false,
                                            includeArguments = true,
                                        ),
                                    ).imports("assertk.assertions.isNull")
                                    .with(
                                        buildString {
                                            append("assertThat(")
                                            append(binaryExpr.rightOperand.sourcePsi!!.text)
                                            append(").isNull()")
                                        },
                                    ).reformat(true)
                                    .build()
                            }

                            UastBinaryOperator.NOT_EQUALS ->
                                fix()
                                    .replace()
                                    .range(
                                        context.getCallLocation(
                                            assertThat,
                                            includeReceiver = false,
                                            includeArguments = true,
                                        ),
                                    ).imports("assertk.assertions.isNotNull")
                                    .with(
                                        buildString {
                                            append("assertThat(")
                                            append(binaryExpr.rightOperand.sourcePsi!!.text)
                                            append(").isNotNull()")
                                        },
                                    ).reformat(true)
                                    .build()

                            else -> null
                        }
                    }

                    else -> null // todo: should this throw?
                }
            }

            binaryExpr.isEqualityComparisonExpr -> {
                val assertion =
                    when (binaryExpr.operator) {
                        UastBinaryOperator.EQUALS -> "isEqualTo"
                        UastBinaryOperator.NOT_EQUALS -> "isNotEqualTo"
                        else -> return null
                    }

                fix()
                    .replace()
                    .range(
                        context.getCallLocation(
                            assertThat,
                            includeReceiver = false,
                            includeArguments = true,
                        ),
                    ).with(
                        buildString {
                            append("assertThat(")
                            if (binaryExpr.leftOperand.isLiteralOrStringTemplate) {
                                append(binaryExpr.rightOperand.sourcePsi!!.text)
                            } else {
                                append(binaryExpr.leftOperand.sourcePsi!!.text)
                            }
                            append(").")
                            append(assertion)
                            append("(")
                            if (binaryExpr.leftOperand.isLiteralOrStringTemplate) {
                                append(binaryExpr.leftOperand.sourcePsi!!.text)
                            } else {
                                append(binaryExpr.rightOperand.sourcePsi!!.text)
                            }
                            append(")")
                        },
                    ).imports("assertk.assertions.$assertion")
                    .reformat(true)
                    .build()
            }

            else -> null
        }
    }

    private fun buildFixForTypeCheck(
        context: JavaContext,
        assertThat: UCallExpression,
        typeCheckExpr: KtIsExpression,
    ): LintFix {
        val assertion = if (typeCheckExpr.isNegated) "isNotInstanceOf" else "isInstanceOf"

        return fix()
            .replace()
            .range(
                context.getCallLocation(
                    assertThat,
                    includeReceiver = false,
                    includeArguments = true,
                ),
            ).imports("assertk.assertions.$assertion")
            .with(
                buildString {
                    append("assertThat(")
                    append(typeCheckExpr.leftHandSide.text)
                    append(").$assertion<")
                    append(typeCheckExpr.typeReference!!.text)
                    append(">()")
                },
            ).reformat(true)
            .build()
    }

    companion object {
        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "UnusedAssertkAssertion",
                briefDescription = "Assertion subjects without assertions never fail a test",
                explanation = """
                    When you create an assertion subject with a method like `assertThat`, you must make assertions on that subject or a test will never fail.
                    """,
                category = Category.CORRECTNESS,
                priority = 6,
                severity = Severity.ERROR,
                implementation =
                    Implementation(
                        UnusedAssertionDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )
    }
}
