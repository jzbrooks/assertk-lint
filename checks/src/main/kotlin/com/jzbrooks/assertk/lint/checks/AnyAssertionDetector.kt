package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.TextFormat
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UastBinaryOperator
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
            override fun visitCallExpression(node: UCallExpression) {
                if (!node.isKotlin) return

                val method = node.resolve() ?: return

                if (method.isAssertThat) {
                    val binaryExprArg =
                        node.valueArguments
                            .map { it.skipParenthesizedExprDown() }
                            .filterIsInstance<UBinaryExpression>()
                    for (argExpr in binaryExprArg) {
                        if (
                            (
                                argExpr.operator == UastBinaryOperator.EQUALS ||
                                    argExpr.operator == UastBinaryOperator.NOT_EQUALS
                            ) &&
                            (
                                (argExpr.leftOperand as? ULiteralExpression)?.isNull == true ||
                                    (argExpr.rightOperand as? ULiteralExpression)?.isNull == true
                            )
                        ) {
                            context.report(
                                NULL_CHECK_ISSUE,
                                context.getCallLocation(
                                    node,
                                    includeReceiver = false,
                                    includeArguments = true,
                                ),
                                NULL_CHECK_ISSUE.getBriefDescription(TextFormat.TEXT),
                            )
                        }
                    }
                }
            }
        }

    companion object {
        @JvmField
        val NULL_CHECK_ISSUE: Issue =
            Issue.create(
                id = "NullCheckAssertion",
                briefDescription =
                    "Use Assert<Any?>.isNull or " +
                        "Assert<Any?>.isNotNull to assert against nullability",
                explanation = """
                    assertk provides `Assert.isNotNull: Assert` which asserts that the value is not null _and_ transforms the assertion subject into an assertion on the non-null type.
                    It also provides `Assert.isNull` for a similar purpose.
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
