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
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import java.util.EnumSet

class KotlinAssertionDetector :
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
                val containingClass = method.containingClass?.qualifiedName ?: return
                if (method.name == "assert" &&
                    (
                        containingClass == "kotlin.PreconditionsKt" ||
                            containingClass == "kotlin.PreconditionsKt__AssertionsJVMKt"
                    )
                ) {
                    context.report(
                        ISSUE,
                        node,
                        context.getCallLocation(
                            node,
                            includeReceiver = false,
                            includeArguments = true,
                        ),
                        ISSUE.getBriefDescription(TextFormat.TEXT),
                        fix()
                            .replace()
                            .reformat(true)
                            .range(
                                context.getCallLocation(
                                    node,
                                    includeReceiver = false,
                                    includeArguments = true,
                                ),
                            ).imports("assertk.assertThat", "assertk.assertions.isTrue")
                            .with(
                                "assertThat(${node.getArgumentForParameter(
                                    0,
                                )!!.sourcePsi!!.text}).isTrue()",
                            ).build(),
                    )
                }
            }
        }

    companion object {
        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "KotlinAssertionUse",
                briefDescription = "Kotlin assertion is used",
                explanation = """
                    assertk assertions are preferred over kotlin assertions for more descriptive and consistent error messages.
                """,
                category = Category.CORRECTNESS,
                priority = 6,
                severity = Severity.WARNING,
                implementation =
                    Implementation(
                        KotlinAssertionDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )
    }
}
