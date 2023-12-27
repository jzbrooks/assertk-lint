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

                // TODO: Should this be so generic?
                if (node.returnType?.canonicalText?.startsWith("assertk.Assert") == true) {
                    for (argExpr in node.valueArguments) {
                        // todo(jzb): Also handle get/getOr* methods
                        if (argExpr is UArrayAccessExpression &&
                            evaluator.getTypeClass(argExpr.receiver.getExpressionType())?.let {
                                // This is goofy, but since the actual underlying definition of
                                // kotlin's built-in map type is defined in the java standard
                                // library we can't check the interface list from that resolved
                                // type. We also can't assume that the receiver's concrete type
                                // is java.util.Map because anyone can implement kotlin's map
                                // interface.
                                //
                                // assertk's API surfaces kotlin's built-in map type, so
                                // Assert<Map<T, U>>.key(key: T): Assert<U> should work for
                                // any type that implements map.
                                //
                                // Unfortunately evaluator.implementsInterface does not return
                                // true if the first argument _is_ the interface (because interfaces
                                // can implement other interfaces).
                                (it.isInterface && it.name == "Map") ||
                                    evaluator.extendsClass(it, "Map")
                            } == true
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
