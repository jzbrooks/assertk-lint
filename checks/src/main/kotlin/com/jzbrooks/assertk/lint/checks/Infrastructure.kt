package com.jzbrooks.assertk.lint.checks

import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.asRecursiveLogString
import org.jetbrains.uast.expressions.UInjectionHost
import org.jetbrains.uast.isNullLiteral
import org.jetbrains.uast.skipParenthesizedExprDown

internal val UExpression.isKotlin: Boolean
    get() = lang === KotlinLanguage.INSTANCE

// todo: can this be done with UAST?
internal val PsiMethod.isAssertThat: Boolean
    get() = name == "assertThat" && containingClass?.qualifiedName == "assertk.AssertKt"

internal val UExpression.isLiteralOrStringTemplate
    get() = this is ULiteralExpression || (this is UInjectionHost && isString)

internal val UBinaryExpression.isNullComparisonExpr: Boolean
    get() =
        operator in setOf(UastBinaryOperator.EQUALS, UastBinaryOperator.NOT_EQUALS) &&
            (leftOperand.isNullLiteral() || rightOperand.isNullLiteral())

internal val UBinaryExpression.isEqualityComparisonExpr: Boolean
    get() =
        operator in setOf(UastBinaryOperator.EQUALS, UastBinaryOperator.NOT_EQUALS) &&
            (!leftOperand.isNullLiteral() && !rightOperand.isNullLiteral())

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
