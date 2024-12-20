package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.DataFlowAnalyzer
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.TextFormat
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getParentOfType
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
                )
            }
        }
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
