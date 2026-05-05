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
import com.intellij.psi.PsiTypes
import org.jetbrains.uast.UCallExpression
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
        if (method.returnType != PsiTypes.booleanType()) return
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
        val clazz = context.evaluator.getTypeClass(node.receiverType)
        return context.evaluator.inheritsFrom(clazz, ASSERTK_ASSERT_FQCN)
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
