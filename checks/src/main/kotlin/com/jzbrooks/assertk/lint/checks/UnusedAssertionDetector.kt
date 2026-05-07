package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.TargetMethodDataFlowAnalyzer
import com.android.tools.lint.client.api.JavaEvaluator
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
import com.android.tools.lint.detector.api.getReceiverOrContainingClass
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.skipParenthesizedExprDown
import org.jetbrains.uast.skipParenthesizedExprUp
import org.jetbrains.uast.visitor.AbstractUastVisitor
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
        if (!context.isTestSource) return

        val containingClass = method.containingClass
        val evaluator = context.evaluator

        if (evaluator.extendsClass(containingClass, "assertk.AssertKt", false)) {
            // Workaround for K2: inside a scope-function lambda
            // (assertThat(x).apply { ... }) the DataFlowAnalyzer fails to fire
            // receiver() for some reason, so calls like isEqualTo(y) inside
            // the lambda look unreachable to the analyzer. If the lambda body
            // contains at least one call expression, treat the chain as used.
            // An empty lambda still falls through to the analyzer, which correctly
            // flags it as unused. It's possible that this implicit receiver issue
            // is a problem with test stubs in this project.
            val parent = skipParenthesizedExprUp(node.uastParent)
            val scopeSelector =
                (parent as? UQualifiedReferenceExpression)
                    ?.takeIf {
                        it.receiver.skipParenthesizedExprDown().sourcePsi === node.sourcePsi
                    }?.selector as? UCallExpression
            if (scopeSelector != null && scopeSelector.scopeLambdaContainsCall(evaluator)) {
                return
            }

            val visitor =
                object : TargetMethodDataFlowAnalyzer(setOf(node)) {
                    override fun isTargetMethod(
                        name: String,
                        method: PsiMethod?,
                    ): Boolean = method != null
                }

            val uElement =
                node.getParentOfType(true, UMethod::class.java, ULambdaExpression::class.java)
            uElement?.accept(visitor)

            if (!visitor.targetReached) {
                val location =
                    context.getCallLocation(
                        call = node,
                        includeReceiver = true,
                        includeArguments = true,
                    )

                context.report(
                    ISSUE,
                    node,
                    location,
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

    private fun UCallExpression.isStandardScopeFunction(): Boolean {
        if (methodName !in SCOPE_FUNCTION_NAMES) return false
        val containingClass = resolve()?.containingClass?.qualifiedName
        return containingClass == "kotlin.StandardKt" ||
            containingClass == "kotlin.StandardKt__StandardKt"
    }

    private fun UCallExpression.scopeLambdaContainsCall(evaluator: JavaEvaluator): Boolean {
        if (!isStandardScopeFunction()) return false

        val lambda =
            valueArguments.lastOrNull()?.skipParenthesizedExprDown() as? ULambdaExpression
                ?: return false
        var found = false
        lambda.body.accept(
            object : AbstractUastVisitor() {
                override fun visitCallExpression(node: UCallExpression): Boolean {
                    val type =
                        evaluator.getClassType(
                            node.resolve()?.getReceiverOrContainingClass(),
                        )
                    found = evaluator.typeMatches(type, "assertk.Assert")
                    return true
                }
            },
        )
        return found
    }

    companion object {
        private val SCOPE_FUNCTION_NAMES = setOf("apply", "also", "run", "with", "let")

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
