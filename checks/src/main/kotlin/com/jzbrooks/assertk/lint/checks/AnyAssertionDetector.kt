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
import com.android.tools.lint.detector.api.TextFormat
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.isNullLiteral
import org.jetbrains.uast.skipParenthesizedExprDown
import java.util.EnumSet

class AnyAssertionDetector :
    Detector(),
    Detector.UastScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(
            UCallExpression::class.java,
        )

    override fun createUastHandler(context: JavaContext) =
        object : UElementHandler() {
            private val UBinaryExpression.isNullComparisonExpr: Boolean
                get() =
                    operator in setOf(UastBinaryOperator.EQUALS, UastBinaryOperator.NOT_EQUALS) &&
                        (leftOperand.isNullLiteral() || rightOperand.isNullLiteral())

            override fun visitCallExpression(node: UCallExpression) {
                if (!node.isKotlin) return

                val method = node.resolve() ?: return

                if (method.isAssertThat) {
                    val binaryExprArg =
                        node.valueArguments
                            .map { it.skipParenthesizedExprDown() }
                            .filterIsInstance<UBinaryExpression>()
                    for (argExpr in binaryExprArg) {
                        if (argExpr.isNullComparisonExpr) {
                            context.report(
                                ISSUE,
                                context.getCallLocation(
                                    node,
                                    includeReceiver = false,
                                    includeArguments = true,
                                ),
                                ISSUE.getBriefDescription(TextFormat.TEXT),
                                getQuickFix(argExpr),
                            )
                        }
                    }
                }
            }

            private fun getQuickFix(nullCheckExpr: UBinaryExpression): LintFix? {
                val assertThatExprReplacement =
                    when {
                        (nullCheckExpr.leftOperand as? ULiteralExpression)?.isNull == true -> {
                            fix()
                                .replace()
                                .range(context.getLocation(nullCheckExpr))
                                .with(nullCheckExpr.rightOperand.sourcePsi!!.text)
                                .reformat(true)
                                .build()
                        }

                        (nullCheckExpr.rightOperand as? ULiteralExpression)?.isNull == true -> {
                            fix()
                                .replace()
                                .range(context.getLocation(nullCheckExpr))
                                .with(nullCheckExpr.leftOperand.sourcePsi!!.text)
                                .reformat(true)
                                .build()
                        }

                        else -> null
                    }

                val assertionCall =
                    nullCheckExpr
                        .getParentOfType<UQualifiedReferenceExpression>()
                        ?.selector as? UCallExpression

                val assertionReplacement =
                    when (nullCheckExpr.operator) {
                        UastBinaryOperator.EQUALS -> {
                            assertionCall?.let { call ->
                                when (call.methodIdentifier?.name) {
                                    "isTrue" ->
                                        fix()
                                            .replace()
                                            .range(
                                                context.getCallLocation(
                                                    call,
                                                    includeReceiver = false,
                                                    includeArguments = true,
                                                ),
                                            ).with("isNull()")
                                            .imports("assertk.assertions.isNull")
                                            .reformat(true)
                                            .build()
                                    "isFalse" ->
                                        fix()
                                            .replace()
                                            .range(
                                                context.getCallLocation(
                                                    call,
                                                    includeReceiver = false,
                                                    includeArguments = true,
                                                ),
                                            ).with("isNotNull()")
                                            .imports("assertk.assertions.isNotNull")
                                            .reformat(true)
                                            .build()
                                    else -> null
                                }
                            }
                        }

                        UastBinaryOperator.NOT_EQUALS -> {
                            assertionCall?.let { call ->
                                when (call.methodIdentifier?.name) {
                                    "isTrue" ->
                                        fix()
                                            .replace()
                                            .range(
                                                context.getCallLocation(
                                                    call,
                                                    includeReceiver = false,
                                                    includeArguments = true,
                                                ),
                                            ).with("isNotNull()")
                                            .imports("assertk.assertions.isNotNull")
                                            .reformat(true)
                                            .build()
                                    "isFalse" ->
                                        fix()
                                            .replace()
                                            .range(
                                                context.getCallLocation(
                                                    call,
                                                    includeReceiver = false,
                                                    includeArguments = true,
                                                ),
                                            ).with("isNull()")
                                            .imports("assertk.assertions.isNull")
                                            .reformat(true)
                                            .build()
                                    else -> null
                                }
                            }
                        }

                        else -> null
                    }

                return if (assertThatExprReplacement != null && assertionReplacement != null) {
                    fix()
                        .name("Replace null comparison with null assertion")
                        .composite(assertThatExprReplacement, assertionReplacement)
                } else {
                    null
                }
            }
        }

    companion object {
        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "NullComparisonAssertion",
                briefDescription = "Use built-in nullability assertions",
                explanation = """
                    assertk provides `Assert.isNotNull: Assert` which asserts that the value is not null _and_ transforms the assertion subject into an assertion on the non-null type. It also provides `Assert.isNull` for a similar purpose.
                """,
                category = Category.USABILITY,
                priority = 6,
                severity = Severity.WARNING,
                implementation =
                    Implementation(
                        AnyAssertionDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )
    }
}
