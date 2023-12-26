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
import com.android.tools.lint.detector.api.isJava
import org.jetbrains.uast.UArrayAccessExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import java.util.EnumSet

class MapAssertionDetector: Detector(), Detector.UastScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(
            UCallExpression::class.java,
        )

    override fun createUastHandler(context: JavaContext) =
        object : UElementHandler() {
            override fun visitCallExpression(node: UCallExpression) {
                // Avoid enforcing assertk use in java
                // sources for mixed language codebases
                if (isJava(node.javaPsi)) return

                val evaluator = context.evaluator

                if (node.returnType?.canonicalText?.startsWith("assertk.Assert") == true) {
                    for (argExpr in node.valueArguments) {
                        if (argExpr is UArrayAccessExpression &&
                            evaluator.inheritsFrom(
                                evaluator.getTypeClass(argExpr.receiver.getExpressionType()),
                                "java.util.Map",
                                false,
                            )
                        ) {
                            context.report(
                                ISSUE,
                                node,
                                context.getLocation(argExpr),
                                ISSUE.getBriefDescription(TextFormat.TEXT),
                            )
                        }
                    }
                }
            }
        }

    companion object {
        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "SuboptimalMapAssertion",
                briefDescription = "Assertk map assertions provide",
                explanation = """
                    AssertJ assertions should not be used in Kotlin tests. Use assertk instead.
                    """,
                category = Category.CORRECTNESS,
                priority = 6,
                severity = Severity.WARNING,
                enabledByDefault = false,
                implementation =
                Implementation(
                    MapAssertionDetector::class.java,
                    EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                ),
            )
    }
}
