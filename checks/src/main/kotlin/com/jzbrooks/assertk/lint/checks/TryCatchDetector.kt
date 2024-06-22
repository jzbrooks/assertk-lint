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
import com.android.tools.lint.detector.api.isJava
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UTryExpression
import org.jetbrains.uast.skipParenthesizedExprDown
import java.util.EnumSet

class TryCatchDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes() = listOf(UTryExpression::class.java)

    override fun createUastHandler(context: JavaContext) =
        object : UElementHandler() {
            override fun visitTryExpression(node: UTryExpression) {
                // Avoid enforcing assertk use in java
                // sources for mixed language codebases
                if (isJava(node.javaPsi)) return

                // This errs on the conservative side since complicated try/catch
                // blocks may have some reason for existing other than the
                // relevant assertion. Simple try/catch who's only purpose is
                // obviously for the test assertion should be replaced. Others
                // might not.
                val tryClause = node.tryClause as? UBlockExpression ?: return
                val catchClause = node.catchClauses.singleOrNull() ?: return
                val catchBody = catchClause.body as? UBlockExpression ?: return
                if (tryClause.expressions.isEmpty()) return

                val assertkCatchAssertionCount =
                    catchBody.expressions
                        .filterIsInstance<UQualifiedReferenceExpression>()
                        .count {
                            val deepestReceiver = it.deepestReceiver

                            deepestReceiver is UCallExpression &&
                                deepestReceiver.methodName == "assertThat" &&
                                context.evaluator.isMemberInClass(
                                    deepestReceiver.resolve(),
                                    "assertk.AssertKt",
                                )
                        }

                val nonFailCallExpressionCount =
                    tryClause.expressions.count {
                        (it.skipParenthesizedExprDown() as? UCallExpression)?.methodName != "fail"
                    }

                val catchBlockHasNoNonAssertionExpressions =
                    catchBody.expressions.isEmpty() ||
                        assertkCatchAssertionCount == catchBody.expressions.size

                if (nonFailCallExpressionCount == 1 && catchBlockHasNoNonAssertionExpressions) {
                    context.report(
                        ISSUE,
                        node,
                        context.getLocation(node),
                        "Use assertk.assertFailure",
                    )
                }
            }
        }

    companion object {
        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "TryCatchAssertion",
                briefDescription = "Use assertFailure for assertions on expected exceptions",
                explanation = """
                    Assertions in catch blocks can be error prone because an explicit `fail` call must immediately follow the call in the try block..
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
