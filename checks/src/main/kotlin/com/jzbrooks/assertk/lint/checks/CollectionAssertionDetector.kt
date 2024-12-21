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
import com.intellij.psi.util.InheritanceUtil
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import java.util.EnumSet

class CollectionAssertionDetector :
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

                val evaluator = context.evaluator

                if (method.isAssertThat) {
                    val propertyAccessExpr =
                        node.valueArguments.firstOrNull()
                            as? UQualifiedReferenceExpression ?: return

                    val propertyReadExpr =
                        propertyAccessExpr.selector
                            as? USimpleNameReferenceExpression ?: return

                    if (propertyReadExpr.resolvedName == "size" &&
                        InheritanceUtil.isInheritorOrSelf(
                            evaluator.getTypeClass(propertyAccessExpr.receiver.getExpressionType()),
                            evaluator.findClass("java.util.Collection"),
                            true,
                        )
                    ) {
                        context.report(
                            SIZE_READ_ISSUE,
                            node,
                            context.getLocation(node),
                            SIZE_READ_ISSUE.getBriefDescription(TextFormat.TEXT),
                        )
                    }
                }
            }
        }

    companion object {
        @JvmField
        val SIZE_READ_ISSUE: Issue =
            Issue.create(
                id = "CollectionSizeAssertion",
                briefDescription = "Use hasSize assertion",
                explanation = """
                    assertk provides `Assert.hasSize(n)` for collection size assertions if not directly indexing.
                """,
                category = Category.PRODUCTIVITY,
                priority = 4,
                severity = Severity.WARNING,
                implementation =
                    Implementation(
                        CollectionAssertionDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )
    }
}
