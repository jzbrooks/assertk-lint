package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.TextFormat
import com.intellij.psi.PsiArrayType
import com.intellij.psi.util.InheritanceUtil
import org.jetbrains.uast.UArrayAccessExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import java.util.EnumSet

class IndexDetector :
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
                    for (argExpr in node.valueArguments) {
                        reportIndexInAssertThatArg(evaluator, argExpr, node)
                    }
                }
            }

            private fun reportIndexInAssertThatArg(
                evaluator: JavaEvaluator,
                argExpr: UExpression,
                node: UCallExpression,
            ) {
                val indexExpr =
                    (argExpr as? UArrayAccessExpression)?.let { expr ->
                        val type = expr.receiver.getExpressionType()
                        val lastIndex = expr.indices.lastOrNull()

                        when {
                            type is PsiArrayType -> IndexExpr(expr.receiver, lastIndex)

                            InheritanceUtil.isInheritorOrSelf(
                                evaluator.getTypeClass(type),
                                evaluator.findClass("java.util.List"),
                                true,
                            ) -> IndexExpr(expr.receiver, lastIndex)

                            else -> null
                        }
                    }

                if (indexExpr != null) {
                    val quickFix =
                        if (indexExpr.indexExpression != null) {
                            fix()
                                .replace()
                                .imports("assertk.assertions.index")
                                .reformat(true)
                                .range(context.getLocation(node))
                                .with(
                                    buildString {
                                        append("assertThat(")
                                        append(indexExpr.collectionExpression.sourcePsi!!.text)
                                        append(").index(")
                                        append(indexExpr.indexExpression.sourcePsi!!.text)
                                        append(')')
                                    },
                                ).build()
                        } else {
                            null
                        }

                    context.report(
                        ISSUE,
                        node,
                        context.getLocation(argExpr),
                        ISSUE.getBriefDescription(TextFormat.TEXT),
                        quickfixData = quickFix,
                    )
                }
            }
        }

    private data class IndexExpr(
        val collectionExpression: UExpression,
        val indexExpression: UExpression?,
    )

    companion object {
        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "UseIndexAssertion",
                briefDescription = "Index with assertk assertions",
                explanation = """
                    assertk provides `Assert.index(i): Assert<T>` (and similar methods on assertion subjects like `Assert.first()` which asserts that the value is present _and_ transforms the assertion subject into an assertion on the value type.
                """,
                category = Category.CORRECTNESS,
                priority = 6,
                severity = Severity.WARNING,
                implementation =
                    Implementation(
                        IndexDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )
    }
}
