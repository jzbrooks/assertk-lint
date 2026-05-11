package com.jzbrooks.assertk.lint.checks

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
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.skipParenthesizedExprDown
import java.util.EnumSet

class SingleAssertionDetector :
    Detector(),
    SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> = listOf("all")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        if (!context.isTestSource) return
        if (context.uastFile?.lang != KotlinLanguage.INSTANCE) return
        if (method.containingClass?.qualifiedName?.startsWith("assertk.") != true) return

        val lambda =
            node.valueArguments.filterIsInstance<ULambdaExpression>().firstOrNull() ?: return
        val body = lambda.body as? UBlockExpression ?: return

        val statements = body.expressions.map { it.unwrap() }
        if (statements.size != 2) return

        var hasSizeCall: UCallExpression? = null
        var transformExpr: UQualifiedReferenceExpression? = null

        for (statement in statements) {
            when {
                statement is UCallExpression &&
                    statement.methodIdentifier?.name == "hasSize" -> {
                    hasSizeCall = statement
                }

                statement is UQualifiedReferenceExpression -> {
                    transformExpr = statement
                }
            }
        }

        if (hasSizeCall == null || transformExpr == null) return

        val hasSizeArg = hasSizeCall.valueArguments.firstOrNull() as? ULiteralExpression ?: return
        if (hasSizeArg.value != 1) return

        val transformCall = transformExpr.deepestReceiver as? UCallExpression ?: return
        val transformName = transformCall.methodIdentifier?.name

        val isApplicableTransform =
            when (transformName) {
                "first" -> true
                "index" -> {
                    val indexArg = transformCall.valueArguments.firstOrNull() as? ULiteralExpression
                    indexArg?.value == 0
                }

                else -> false
            }

        if (!isApplicableTransform) return

        val selectors = mutableListOf<String>()
        var current: UExpression = transformExpr
        while (current is UQualifiedReferenceExpression) {
            val selectorText = current.selector.sourcePsi?.text ?: return
            selectors.add(0, selectorText)
            current = current.receiver.skipParenthesizedExprDown()
        }
        val restText = selectors.joinToString(".")

        val callLocation =
            context.getCallLocation(
                node,
                includeReceiver = false,
                includeArguments = true,
            )

        val quickFix =
            fix()
                .replace()
                .imports("assertk.assertions.single")
                .reformat(true)
                .range(callLocation)
                .with("single().$restText")
                .build()

        context.report(
            ISSUE,
            node,
            callLocation,
            ISSUE.getBriefDescription(TextFormat.TEXT),
            quickFix,
        )
    }

    private fun UExpression.unwrap(): UExpression {
        var current: UExpression = this
        while (true) {
            current = current.skipParenthesizedExprDown()
            val returnValue = (current as? UReturnExpression)?.returnExpression ?: break
            current = returnValue
        }
        return current
    }

    companion object {
        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "UseSingleAssertion",
                briefDescription = "Use single assertion for one-element collection assertions",
                explanation = """
                    assertk's `single()` asserts that the collection has exactly one element \
                    and transforms the assertion subject to that element. Prefer `single()` \
                    over combining `hasSize(1)` with `index(0)` or `first()` inside an `all { }` block.
                """,
                category = Category.PRODUCTIVITY,
                priority = 4,
                severity = Severity.WARNING,
                implementation =
                    Implementation(
                        SingleAssertionDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )
    }
}
