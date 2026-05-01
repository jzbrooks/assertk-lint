package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.skipParenthesizedExprDown
import java.util.EnumSet

class AssertThatEqualsDetector :
    Detector(),
    SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> = listOf("equals")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        if (!context.isTestSource) return
        if (node.valueArgumentCount != 1) return
        if (!isAssertkAssertReceiver(context, node)) return

        val argText =
            node.valueArguments
                .first()
                .sourcePsi
                ?.text ?: "..."
        val receiverText =
            node.receiver
                ?.skipParenthesizedExprDown()
                ?.sourcePsi
                ?.text
                ?.trim()
                ?.takeUnless { it.contains('\n') }
        val originalCallText = receiverText?.let { "$it.equals($argText)" } ?: "equals($argText)"

        context.report(
            ISSUE,
            node,
            context.getLocation(node),
            "Replace `$originalCallText` with `assertThat(...).isEqualTo(...)`.",
            buildFix(context, node),
        )
    }

    private fun isAssertkAssertReceiver(
        context: JavaContext,
        node: UCallExpression,
    ): Boolean {
        val receiverExpr = node.receiver?.skipParenthesizedExprDown()

        if (receiverExpr != null) {
            val receiverType = receiverExpr.getExpressionType() ?: return false
            val receiverClass = context.evaluator.getTypeClass(receiverType) ?: return false
            return context.evaluator.extendsClass(receiverClass, ASSERTK_ASSERT_FQCN, false)
        }

        var cursor: UElement? = node.uastParent
        while (cursor != null) {
            if (cursor is ULambdaExpression) {
                val enclosingCall = cursor.uastParent as? UCallExpression
                val callReceiver = enclosingCall?.receiver?.skipParenthesizedExprDown()
                if (callReceiver != null) {
                    val receiverType = callReceiver.getExpressionType() ?: break
                    val receiverClass = context.evaluator.getTypeClass(receiverType) ?: break
                    return context.evaluator.extendsClass(receiverClass, ASSERTK_ASSERT_FQCN, false)
                }
                break
            }
            cursor = cursor.uastParent
        }

        return false
    }

    private fun buildFix(
        context: JavaContext,
        node: UCallExpression,
    ): LintFix? {
        val argument =
            node.valueArguments
                .firstOrNull()
                ?.sourcePsi
                ?.text ?: return null

        return fix()
            .name("Replace equals with isEqualTo")
            .replace()
            .range(
                context.getCallLocation(
                    call = node,
                    includeReceiver = false,
                    includeArguments = true,
                ),
            ).with("isEqualTo($argument)")
            .imports("assertk.assertions.isEqualTo")
            .reformat(true)
            .build()
    }

    companion object {
        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "AssertThatEqualsUsage",
                briefDescription = "Use isEqualTo assertions instead of equals",
                explanation = (
                    "Calling `equals` on an `assertk.Assert` receiver does not " +
                        "perform an assertion and can let tests pass unexpectedly. " +
                        "Replace it with `isEqualTo`."
                ),
                category = Category.CORRECTNESS,
                priority = 6,
                severity = Severity.ERROR,
                implementation =
                    Implementation(
                        AssertThatEqualsDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )

        private const val ASSERTK_ASSERT_FQCN = "assertk.Assert"
    }
}
