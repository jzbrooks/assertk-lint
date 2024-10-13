package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.detector.api.isKotlin
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.asRecursiveLogString
import org.jetbrains.uast.skipParenthesizedExprDown

internal val UExpression.isKotlin: Boolean
    get() {
        val sourcePsi = sourcePsi ?: return false
        return isKotlin(sourcePsi.language)
    }

// todo: can this be done with UAST?
internal val PsiMethod.isAssertThat: Boolean
    get() = name == "assertThat" && containingClass?.qualifiedName == "assertk.AssertKt"

/**
 * Gets the receiver of the expression which is evaluated first at runtime.
 *
 * Example (using [UElement.asRecursiveLogString]):
 * ```text
 * UQualifiedReferenceExpression
 *     UQualifiedReferenceExpression
 *         UCallExpression (kind = UastCallKind(name='method_call'), argCount = 1)) <-- this is the receiver we care about
 *             UIdentifier (Identifier (assertThat))
 *             USimpleNameReferenceExpression (identifier = assertThat, resolvesTo = null)
 *             USimpleNameReferenceExpression (identifier = e)
 *         UCallExpression (kind = UastCallKind(name='method_call'), argCount = 1))
 *             UIdentifier (Identifier (prop))
 *             USimpleNameReferenceExpression (identifier = prop, resolvesTo = null)
 *             UCallableReferenceExpression (name = message)
 *     UCallExpression (kind = UastCallKind(name='method_call'), argCount = 1))
 *         UIdentifier (Identifier (isEqualTo))
 *         USimpleNameReferenceExpression (identifier = <anonymous class>, resolvesTo = null)
 *         ULiteralExpression (value = "Boom!")
 * ```
 */
internal val UQualifiedReferenceExpression.deepestReceiver: UExpression
    get() {
        var currentReceiver = receiver.skipParenthesizedExprDown()
        while (currentReceiver is UQualifiedReferenceExpression) {
            currentReceiver = currentReceiver.receiver.skipParenthesizedExprDown()
        }
        return currentReceiver.skipParenthesizedExprDown()
    }
