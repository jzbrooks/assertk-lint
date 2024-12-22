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
import com.intellij.psi.PsiType
import com.intellij.psi.util.InheritanceUtil
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UCallableReferenceExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.skipParenthesizedExprUp
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

                val firstArgExpr = node.valueArguments.firstOrNull()
                if (method.isAssertThat && firstArgExpr != null) {
                    reportSizeReadEquality(evaluator, firstArgExpr, node)
                }
            }

            private fun reportSizeReadEquality(
                evaluator: JavaEvaluator,
                argExpr: UExpression,
                node: UCallExpression,
            ) {
                val sizeRead = getReceiverForSizeRead(evaluator, argExpr)
                if (sizeRead != null) {
                    val parentExpr =
                        skipParenthesizedExprUp(node.uastParent)
                            as? UQualifiedReferenceExpression

                    val callExpr = parentExpr?.selector as? UCallExpression
                    if (callExpr?.methodIdentifier?.name == "isEqualTo") {
                        val containingClassName = callExpr.resolve()?.containingClass?.qualifiedName
                        if (containingClassName == "assertk.assertions.AnyKt") {
                            val callArgument = callExpr.valueArguments.firstOrNull()
                            val quickFix =
                                if (sizeRead.collectionExpr != null && callArgument != null) {
                                    fix()
                                        .replace()
                                        .imports("assertk.assertions.hasSize")
                                        .reformat(true)
                                        .range(context.getLocation(parentExpr))
                                        .with(
                                            buildString {
                                                append("assertThat(")
                                                append(sizeRead.collectionExpr.sourcePsi!!.text)
                                                append(").hasSize(")
                                                append(callArgument.sourcePsi!!.text)
                                                append(')')
                                            },
                                        ).build()
                                } else {
                                    null
                                }

                            context.report(
                                SIZE_READ_ISSUE,
                                node,
                                context.getLocation(node),
                                SIZE_READ_ISSUE.getBriefDescription(TextFormat.TEXT),
                                quickFix,
                            )
                        }
                    }
                }
            }

            private fun JavaEvaluator.isSizeReadEqualTo(
                propertyAccessExpr: UQualifiedReferenceExpression,
            ): Boolean {
                val propertyReadExpr =
                    propertyAccessExpr.selector
                        as? USimpleNameReferenceExpression ?: return false

                val parentExpr =
                    skipParenthesizedExprUp(propertyAccessExpr.uastParent)
                        as? UQualifiedReferenceExpression

                val callExpr = parentExpr?.selector as? UCallExpression

                return callExpr?.methodIdentifier?.name == "isEqualTo" &&
                    propertyReadExpr.resolvedName == "size" &&
                    InheritanceUtil.isInheritorOrSelf(
                        getTypeClass(propertyAccessExpr.receiver.getExpressionType()),
                        findClass("java.util.Collection"),
                        true,
                    )
            }

            private fun getReceiverForSizeRead(
                evaluator: JavaEvaluator,
                expression: UExpression,
            ): SizeRead? =
                when (expression) {
                    is UCallableReferenceExpression -> {
                        if (evaluator.isCollectionType(expression.qualifierType) &&
                            expression.callableName == "size"
                        ) {
                            // For some reason this is always null
                            SizeRead(expression.qualifierExpression)
                        } else {
                            null
                        }
                    }

                    is UQualifiedReferenceExpression -> {
                        val call = expression.selector
                        call is Collection<*>
                        if (call is USimpleNameReferenceExpression) {
                            if (evaluator.isCollectionType(
                                    expression.receiver.getExpressionType(),
                                ) &&
                                call.identifier == "size"
                            ) {
                                SizeRead(expression.receiver)
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                    }

                    else -> null
                }

            private fun JavaEvaluator.isCollectionType(psiType: PsiType?): Boolean {
                val type = psiType ?: return false
                val receiverType = getTypeClass(type) ?: return false

                return InheritanceUtil.isInheritorOrSelf(
                    receiverType,
                    findClass("java.util.Collection"),
                    true,
                )
            }
        }

    data class SizeRead(
        val collectionExpr: UExpression?,
    )

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
