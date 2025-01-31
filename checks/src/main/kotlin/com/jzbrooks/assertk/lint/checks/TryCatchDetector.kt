package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UTryExpression
import org.jetbrains.uast.getUCallExpression
import org.jetbrains.uast.skipParenthesizedExprDown
import org.jetbrains.uast.skipParenthesizedExprUp
import java.util.EnumSet

class TryCatchDetector :
    Detector(),
    SourceCodeScanner {
    override fun getApplicableUastTypes() = listOf(UTryExpression::class.java)

    override fun createUastHandler(context: JavaContext) =
        object : UElementHandler() {
            override fun visitTryExpression(node: UTryExpression) {
                if (!node.isKotlin) return

                // This errs on the conservative side since complicated try/catch
                // blocks may have some reason for existing other than the
                // relevant assertion. Simple try/catch who's only purpose is
                // obviously for the test assertion should be replaced. Others
                // might not.
                val tryClause = node.tryClause as? UBlockExpression ?: return
                val catchClause = node.catchClauses.singleOrNull() ?: return
                val catchBody = catchClause.body as? UBlockExpression ?: return
                if (tryClause.expressions.isEmpty()) return

                val nonFailCallExpressionCount =
                    tryClause.expressions.count {
                        it.skipParenthesizedExprDown().getUCallExpression()?.methodName != "fail"
                    }

                if (nonFailCallExpressionCount == 1) {
                    val assertkCatchAssertionCount =
                        catchBody.expressions
                            .filterIsInstance<UQualifiedReferenceExpression>()
                            .count {
                                val deepestReceiver = it.deepestReceiver
                                val firstCall =
                                    if (deepestReceiver is UCallExpression) {
                                        deepestReceiver
                                    } else {
                                        skipParenthesizedExprUp(it.deepestReceiver)
                                            .getUCallExpression()
                                    }

                                firstCall != null &&
                                    firstCall.methodName == "assertThat" &&
                                    context.evaluator.isMemberInClass(
                                        firstCall.resolve(),
                                        "assertk.AssertKt",
                                    )
                            }

                    val onlyAssertsInCatchBlock =
                        assertkCatchAssertionCount > 0 &&
                            assertkCatchAssertionCount == catchBody.expressions.size

                    if (onlyAssertsInCatchBlock) {
                        context.report(
                            ISSUE,
                            node,
                            context.getLocation(node),
                            "Use `assertk.assertFailure`",
                        )
                    }
                }
            }
        }

    companion object {
        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "TryCatchAssertion",
                briefDescription = "Use `assertFailure` for expected exceptions",
                explanation = """
                    Assertions in catch blocks can be error prone because an explicit `fail` call must immediately follow the call in the try block. Note: This detector will not raise issues for complicated try/catch blocks since they may have some reason to exist beyond asserting on exception behavior.
                    """,
                category = Category.CORRECTNESS,
                priority = 8,
                severity = Severity.WARNING,
                implementation =
                    Implementation(
                        TryCatchDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )
    }
}
