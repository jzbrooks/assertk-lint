package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.TextFormat
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiWildcardType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.getOutermostQualified
import org.jetbrains.uast.getQualifiedChain
import org.jetbrains.uast.skipParenthesizedExprDown
import java.util.EnumSet

class BooleanExpressionSubjectDetector :
    Detector(),
    Detector.UastScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(
            UCallExpression::class.java,
        )

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        if (!context.isTestSource) return null
        if (context.uastFile?.lang != KotlinLanguage.INSTANCE) return null

        return object : UElementHandler() {
            override fun visitCallExpression(node: UCallExpression) {
                val method = node.resolve() ?: return

                if (node.methodName == "isEqualTo") {
                    visitBooleanEquality(node)
                    return
                }

                if (method.isAssertThat) {
                    val binaryExprArg =
                        node.valueArguments
                            .map { it.skipParenthesizedExprDown() }
                            .filterIsInstance<UBinaryExpression>()

                    for (argExpr in binaryExprArg) {
                        when {
                            argExpr.isNullComparisonExpr ->
                                context.report(
                                    NULL_EXPR_ISSUE,
                                    context.getCallLocation(
                                        node,
                                        includeReceiver = false,
                                        includeArguments = true,
                                    ),
                                    NULL_EXPR_ISSUE.getBriefDescription(TextFormat.TEXT),
                                    buildNullExprQuickFix(argExpr, node),
                                )

                            argExpr.isEqualityComparisonExpr ->
                                context.report(
                                    EQUALITY_EXPR_ISSUE,
                                    context.getCallLocation(
                                        node,
                                        includeReceiver = false,
                                        includeArguments = true,
                                    ),
                                    EQUALITY_EXPR_ISSUE.getBriefDescription(TextFormat.TEXT),
                                    buildEqualExprQuickFix(argExpr, node),
                                )
                        }
                    }
                }
            }

            private fun visitBooleanEquality(node: UCallExpression) {
                val expected = node.booleanLiteralArgument() ?: return
                val assertClass = context.evaluator.getTypeClass(node.receiverType)
                if (!context.evaluator.inheritsFrom(assertClass, "assertk.Assert")) return

                // Null/equality comparison subjects are fixed in one step by the other issues
                val subject =
                    node
                        .getOutermostQualified()
                        ?.getQualifiedChain()
                        ?.filterIsInstance<UCallExpression>()
                        ?.firstOrNull { it.resolve()?.isAssertThat == true }
                        ?.valueArguments
                        ?.firstOrNull()
                        ?.skipParenthesizedExprDown()
                if (subject is UBinaryExpression &&
                    (subject.isNullComparisonExpr || subject.isEqualityComparisonExpr)
                ) {
                    return
                }

                val subjectType =
                    (node.receiverType as? PsiClassType)?.parameters?.firstOrNull()?.let {
                        (it as? PsiWildcardType)?.bound ?: it
                    }
                val nullable =
                    subjectType?.annotations?.any {
                        it.qualifiedName == "org.jetbrains.annotations.Nullable"
                    } == true

                val assertion = if (expected) "isTrue" else "isFalse"
                val imports =
                    if (nullable) {
                        arrayOf("assertk.assertions.isNotNull", "assertk.assertions.$assertion")
                    } else {
                        arrayOf("assertk.assertions.$assertion")
                    }
                val replacement = if (nullable) "isNotNull().$assertion()" else "$assertion()"

                val location =
                    context.getCallLocation(node, includeReceiver = false, includeArguments = true)
                context.report(
                    BOOLEAN_EQUALITY_ISSUE,
                    location,
                    BOOLEAN_EQUALITY_ISSUE.getBriefDescription(TextFormat.TEXT),
                    fix()
                        .name("Replace with $replacement")
                        .replace()
                        .range(location)
                        .with(replacement)
                        .imports(*imports)
                        .reformat(true)
                        .build(),
                )
            }

            private fun buildNullExprQuickFix(
                nullCheckExpr: UBinaryExpression,
                call: UCallExpression,
            ): LintFix? {
                val assertThatExprReplacement =
                    when {
                        (nullCheckExpr.leftOperand as? ULiteralExpression)?.isNull == true -> {
                            fix()
                                .replace()
                                .range(context.getLocation(nullCheckExpr))
                                .with(nullCheckExpr.rightOperand.sourcePsi!!.text)
                                .reformat(true)
                                .build()
                        }

                        (nullCheckExpr.rightOperand as? ULiteralExpression)?.isNull == true -> {
                            fix()
                                .replace()
                                .range(context.getLocation(nullCheckExpr))
                                .with(nullCheckExpr.leftOperand.sourcePsi!!.text)
                                .reformat(true)
                                .build()
                        }

                        else -> null
                    }

                val assertionCall =
                    call
                        .getOutermostQualified()
                        .getQualifiedChain()
                        .lastOrNull() as? UCallExpression

                val assertionReplacement =
                    when (nullCheckExpr.operator) {
                        UastBinaryOperator.EQUALS -> {
                            assertionCall?.let { call ->
                                when (call.booleanAssertionName()) {
                                    "isTrue" ->
                                        fix()
                                            .replace()
                                            .range(
                                                context.getCallLocation(
                                                    call,
                                                    includeReceiver = false,
                                                    includeArguments = true,
                                                ),
                                            ).with("isNull()")
                                            .imports("assertk.assertions.isNull")
                                            .reformat(true)
                                            .build()

                                    "isFalse" ->
                                        fix()
                                            .replace()
                                            .range(
                                                context.getCallLocation(
                                                    call,
                                                    includeReceiver = false,
                                                    includeArguments = true,
                                                ),
                                            ).with("isNotNull()")
                                            .imports("assertk.assertions.isNotNull")
                                            .reformat(true)
                                            .build()

                                    else -> null
                                }
                            }
                        }

                        UastBinaryOperator.NOT_EQUALS -> {
                            assertionCall?.let { call ->
                                when (call.booleanAssertionName()) {
                                    "isTrue" ->
                                        fix()
                                            .replace()
                                            .range(
                                                context.getCallLocation(
                                                    call,
                                                    includeReceiver = false,
                                                    includeArguments = true,
                                                ),
                                            ).with("isNotNull()")
                                            .imports("assertk.assertions.isNotNull")
                                            .reformat(true)
                                            .build()

                                    "isFalse" ->
                                        fix()
                                            .replace()
                                            .range(
                                                context.getCallLocation(
                                                    call,
                                                    includeReceiver = false,
                                                    includeArguments = true,
                                                ),
                                            ).with("isNull()")
                                            .imports("assertk.assertions.isNull")
                                            .reformat(true)
                                            .build()

                                    else -> null
                                }
                            }
                        }

                        else -> null
                    }

                return if (assertThatExprReplacement != null && assertionReplacement != null) {
                    fix()
                        .name("Replace null comparison with null assertion")
                        .composite(assertThatExprReplacement, assertionReplacement)
                } else {
                    null
                }
            }

            private fun buildEqualExprQuickFix(
                equalityExpr: UBinaryExpression,
                call: UCallExpression,
            ): LintFix? {
                val (assertThatExprReplacement, expectsExpr) =
                    when {
                        equalityExpr.leftOperand.isLiteralOrStringTemplate ->
                            fix()
                                .replace()
                                .range(context.getLocation(equalityExpr))
                                .with(equalityExpr.rightOperand.sourcePsi!!.text)
                                .reformat(true)
                                .build() to equalityExpr.leftOperand

                        else ->
                            fix()
                                .replace()
                                .range(context.getLocation(equalityExpr))
                                .with(equalityExpr.leftOperand.sourcePsi!!.text)
                                .reformat(true)
                                .build() to equalityExpr.rightOperand
                    }

                val assertionCall =
                    call
                        .getOutermostQualified()
                        .getQualifiedChain()
                        .lastOrNull() as? UCallExpression

                val assertionReplacement =
                    when (equalityExpr.operator) {
                        UastBinaryOperator.EQUALS -> {
                            assertionCall?.let {
                                when (it.booleanAssertionName()) {
                                    "isTrue" ->
                                        fix()
                                            .replace()
                                            .range(
                                                context.getCallLocation(
                                                    it,
                                                    includeReceiver = false,
                                                    includeArguments = true,
                                                ),
                                            ).with(
                                                "isEqualTo(${expectsExpr.sourcePsi!!.text})",
                                            ).imports("assertk.assertions.isEqualTo")
                                            .reformat(true)
                                            .build()

                                    "isFalse" ->
                                        fix()
                                            .replace()
                                            .range(
                                                context.getCallLocation(
                                                    it,
                                                    includeReceiver = false,
                                                    includeArguments = true,
                                                ),
                                            ).with(
                                                "isNotEqualTo(${expectsExpr.sourcePsi!!.text})",
                                            ).imports("assertk.assertions.isNotEqualTo")
                                            .reformat(true)
                                            .build()

                                    else -> null
                                }
                            }
                        }

                        UastBinaryOperator.NOT_EQUALS -> {
                            assertionCall?.let {
                                when (it.booleanAssertionName()) {
                                    "isTrue" ->
                                        fix()
                                            .replace()
                                            .range(
                                                context.getCallLocation(
                                                    it,
                                                    includeReceiver = false,
                                                    includeArguments = true,
                                                ),
                                            ).with(
                                                "isNotEqualTo(${expectsExpr.sourcePsi!!.text})",
                                            ).imports("assertk.assertions.isNotEqualTo")
                                            .reformat(true)
                                            .build()

                                    "isFalse" ->
                                        fix()
                                            .replace()
                                            .range(
                                                context.getCallLocation(
                                                    it,
                                                    includeReceiver = false,
                                                    includeArguments = true,
                                                ),
                                            ).with(
                                                "isEqualTo(${expectsExpr.sourcePsi!!.text})",
                                            ).imports("assertk.assertions.isEqualTo")
                                            .reformat(true)
                                            .build()

                                    else -> null
                                }
                            }
                        }

                        else -> null
                    }

                return if (assertionReplacement != null) {
                    fix()
                        .name("Replace equality check with equality assertion")
                        .composite(assertThatExprReplacement, assertionReplacement)
                } else {
                    null
                }
            }
        }
    }

    private fun UCallExpression.booleanLiteralArgument(): Boolean? =
        if (methodName == "isEqualTo" && valueArgumentCount == 1) {
            val literal = valueArguments.single().skipParenthesizedExprDown() as? ULiteralExpression
            literal?.value as? Boolean
        } else {
            null
        }

    /** Treats `isEqualTo(true)`/`isEqualTo(false)` as `isTrue()`/`isFalse()` so fixes land in one step. */
    private fun UCallExpression.booleanAssertionName(): String? =
        when (methodIdentifier?.name) {
            "isTrue", "isFalse" -> methodIdentifier?.name
            else -> booleanLiteralArgument()?.let { if (it) "isTrue" else "isFalse" }
        }

    companion object {
        @JvmField
        val BOOLEAN_EQUALITY_ISSUE: Issue =
            Issue.create(
                id = "BooleanEqualityAssertion",
                briefDescription = "Use boolean assertions",
                explanation = """
                    assertk provides `Assert<Boolean>.isTrue` and `Assert<Boolean>.isFalse`, which read better than `isEqualTo(true)` and `isEqualTo(false)`. For nullable subjects, chain `isNotNull()` first.
                """,
                category = Category.USABILITY,
                priority = 6,
                severity = Severity.WARNING,
                implementation =
                    Implementation(
                        BooleanExpressionSubjectDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )

        @JvmField
        val NULL_EXPR_ISSUE: Issue =
            Issue.create(
                id = "NullComparisonAssertion",
                briefDescription = "Use built-in nullability assertions",
                explanation = """
                    assertk provides `Assert.isNotNull: Assert` which asserts that the value is not null _and_ transforms the assertion subject into an assertion on the non-null type. It also provides `Assert.isNull` for a similar purpose.
                """,
                category = Category.USABILITY,
                priority = 6,
                severity = Severity.WARNING,
                implementation =
                    Implementation(
                        BooleanExpressionSubjectDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )

        @JvmField
        val EQUALITY_EXPR_ISSUE: Issue =
            Issue.create(
                id = "EqualityComparisonAssertion",
                briefDescription = "Use equality assertions",
                explanation = """
                    assertk provides `Assert.isEqualTo`, `Assert.isNotEqualTo`, and `Assert.isDataClassEqualTo`
                """,
                category = Category.USABILITY,
                priority = 6,
                severity = Severity.WARNING,
                implementation =
                    Implementation(
                        BooleanExpressionSubjectDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )
    }
}
