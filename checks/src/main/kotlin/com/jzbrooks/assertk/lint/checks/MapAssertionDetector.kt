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
import com.android.tools.lint.detector.api.isJava
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.uast.UArrayAccessExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UCallableReferenceExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.skipParenthesizedExprUp
import java.util.EnumSet

class MapAssertionDetector : Detector(), Detector.UastScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(
            UCallExpression::class.java,
        )

    override fun createUastHandler(context: JavaContext) =
        object : UElementHandler() {
            private val PsiMethod.isAssertThat: Boolean
                get() = name == "assertThat" && containingClass?.qualifiedName == "assertk.AssertKt"

            override fun visitCallExpression(node: UCallExpression) {
                // Avoid enforcing assertk use in java
                // sources for mixed language codebases
                if (isJava(node.javaPsi)) return
                val method = node.resolve() ?: return

                val evaluator = context.evaluator

                if (method.isAssertThat) {
                    for (argExpr in node.valueArguments) {
                        reportAnyDirectMapRead(evaluator, argExpr, node)
                        reportAnyKeyAbsenceCheck(evaluator, argExpr, node)
                        reportAnyKeyPresenceCheck(evaluator, argExpr, node)
                    }
                }
            }

            private fun reportAnyDirectMapRead(
                evaluator: JavaEvaluator,
                argExpr: UExpression,
                node: UCallExpression,
            ) {
                val isMapRead =
                    when (argExpr) {
                        is UArrayAccessExpression -> {
                            evaluator.isMapType(argExpr.receiver.getExpressionType())
                        }

                        is UQualifiedReferenceExpression -> {
                            val call = argExpr.selector

                            if (call is UCallExpression) {
                                evaluator.isMapType(call.receiverType) &&
                                    call.methodName in MAP_ACCESSOR_METHOD_NAMES
                            } else {
                                false
                            }
                        }

                        else -> false
                    }

                if (isMapRead) {
                    context.report(
                        DIRECT_READ_ISSUE,
                        node,
                        context.getLocation(argExpr),
                        DIRECT_READ_ISSUE.getBriefDescription(TextFormat.TEXT),
                    )
                }
            }

            private fun reportAnyKeyAbsenceCheck(
                evaluator: JavaEvaluator,
                argExpr: UExpression,
                node: UCallExpression,
            ) {
                var mapExpression: UExpression? = null

                val isKeyRead =
                    when (argExpr) {
                        is UCallableReferenceExpression -> {
                            if (evaluator.isMapType(argExpr.qualifierType) &&
                                argExpr.callableName == "keys"
                            ) {
                                mapExpression = argExpr.qualifierExpression
                                true
                            } else {
                                false
                            }
                        }

                        is UQualifiedReferenceExpression -> {
                            val call = argExpr.selector

                            if (call is USimpleNameReferenceExpression) {
                                if (evaluator.isMapType(argExpr.receiver.getExpressionType()) &&
                                    call.identifier == "keys"
                                ) {
                                    mapExpression = argExpr.receiver
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        }

                        else -> false
                    }

                if (isKeyRead) {
                    val parentExpr =
                        skipParenthesizedExprUp(node.uastParent)
                            as? UQualifiedReferenceExpression

                    val callExpr = parentExpr?.selector as? UCallExpression
                    if (callExpr?.methodIdentifier?.name == "doesNotContain") {
                        val containingClassName = callExpr.resolve()?.containingClass?.qualifiedName

                        if (containingClassName == "assertk.assertions.IterableKt") {
                            context.report(
                                KEYS_SET_CHECK,
                                node,
                                context.getLocation(parentExpr),
                                KEYS_SET_CHECK.getBriefDescription(TextFormat.TEXT),
                                quickfixData =
                                    mapExpression?.let { mapExpr ->
                                        callExpr.valueArguments.firstOrNull()?.let { keyExpr ->
                                            fix().replace()
                                                .imports("assertk.assertions.doesNotContainKey")
                                                .reformat(true)
                                                .range(context.getLocation(parentExpr))
                                                .with(
                                                    buildString {
                                                        append("assertThat(")
                                                        append(mapExpr.sourcePsi!!.text)
                                                        append(").doesNotContainKey(")

                                                        // For some reason KtLiteralStringTemplateEntry
                                                        // does not include string delimiters in its
                                                        // text
                                                        val keyExprPsi = keyExpr.sourcePsi!!
                                                        if (keyExprPsi is KtLiteralStringTemplateEntry) {
                                                            append('"')
                                                            append(keyExprPsi.text)
                                                            append('"')
                                                        } else {
                                                            append(keyExprPsi.text)
                                                        }
                                                        append(')')
                                                    },
                                                )
                                                .build()
                                        }
                                    },
                            )
                        }
                    }
                }
            }

            private fun reportAnyKeyPresenceCheck(
                evaluator: JavaEvaluator,
                argExpr: UExpression,
                node: UCallExpression,
            ) {
                var mapExpression: UExpression? = null

                val isKeyRead =
                    when (argExpr) {
                        is UCallableReferenceExpression -> {
                            if (evaluator.isMapType(argExpr.qualifierType) &&
                                argExpr.callableName == "keys"
                            ) {
                                mapExpression = argExpr.qualifierExpression
                                true
                            } else {
                                false
                            }
                        }

                        is UQualifiedReferenceExpression -> {
                            val call = argExpr.selector

                            if (call is USimpleNameReferenceExpression) {
                                if (evaluator.isMapType(argExpr.receiver.getExpressionType()) &&
                                    call.identifier == "keys"
                                ) {
                                    mapExpression = argExpr.receiver
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        }

                        else -> false
                    }

                if (isKeyRead) {
                    val parentExpr =
                        skipParenthesizedExprUp(node.uastParent)
                            as? UQualifiedReferenceExpression

                    val callExpr = parentExpr?.selector as? UCallExpression
                    if (callExpr?.methodIdentifier?.name == "contains") {
                        val containingClassName = callExpr.resolve()?.containingClass?.qualifiedName

                        if (containingClassName == "assertk.assertions.IterableKt") {
                            context.report(
                                KEYS_SET_CHECK,
                                node,
                                context.getLocation(parentExpr),
                                KEYS_SET_CHECK.getBriefDescription(TextFormat.TEXT),
                                quickfixData =
                                mapExpression?.let { mapExpr ->
                                    callExpr.valueArguments.firstOrNull()?.let { keyExpr ->
                                        fix().replace()
                                            .imports("assertk.assertions.key")
                                            .reformat(true)
                                            .range(context.getLocation(parentExpr))
                                            .with(
                                                buildString {
                                                    append("assertThat(")
                                                    append(mapExpr.sourcePsi!!.text)
                                                    append(").key(")

                                                    // For some reason KtLiteralStringTemplateEntry
                                                    // does not include string delimiters in its
                                                    // text
                                                    val keyExprPsi = keyExpr.sourcePsi!!
                                                    if (keyExprPsi is KtLiteralStringTemplateEntry) {
                                                        append('"')
                                                        append(keyExprPsi.text)
                                                        append('"')
                                                    } else {
                                                        append(keyExprPsi.text)
                                                    }
                                                    append(')')
                                                },
                                            )
                                            .build()
                                    }
                                },
                            )
                        }
                    }
                }
            }

            private fun JavaEvaluator.isMapType(psiType: PsiType?): Boolean {
                val type = psiType ?: return false
                val receiverType = getTypeClass(type) ?: return false

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
                return (receiverType.isInterface && receiverType.name == "Map") ||
                    implementsInterface(receiverType, "Map")
            }
        }

    companion object {
        private val MAP_ACCESSOR_METHOD_NAMES =
            setOf(
                "getValue",
                "getOrElse",
                "getOrDefault",
                "get",
            )

        @JvmField
        val DIRECT_READ_ISSUE: Issue =
            Issue.create(
                id = "MapValueAssertion",
                briefDescription =
                    "assertk provides Assert<Map<U, V>>.key(U) to" +
                        " make assertions on particular map values",
                explanation = """
                    assertk provides `Assert<Map<U, V>>.key(U): Assert<V>` which asserts that the value is present _and_ transforms the assertion subject into an assertion on the value type.
                """,
                category = Category.CORRECTNESS,
                priority = 6,
                severity = Severity.WARNING,
                implementation =
                    Implementation(
                        MapAssertionDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )

        @JvmField
        val KEYS_SET_CHECK: Issue =
            Issue.create(
                id = "KeysSetCheck",
                briefDescription =
                    "assertk provides Assert<Map<U, V>>.doesNotContainKey() to" +
                        " assert that a key is not present in a map",
                explanation = """
                    assertk provides `Assert<Map<U, V>>.doesNotContainKey(U)` which asserts that the key is not present in the map with a consistent assertion message.
                """,
                category = Category.USABILITY,
                priority = 6,
                severity = Severity.WARNING,
                implementation =
                    Implementation(
                        MapAssertionDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                    ),
            )
    }
}
