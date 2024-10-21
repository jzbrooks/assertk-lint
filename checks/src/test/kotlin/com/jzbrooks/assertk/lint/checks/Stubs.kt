package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin

val assertkStub =
    """
     package assertk

     import java.lang.AssertionError

    // This name is a hack to get the test infrastructure to correctly
     // name this test stub file's class to AssertkKt
     class Assert<T> {

     }

     fun <T> assertThat(subject: T?): Assert<T> {

     }

     fun fail() {
         throw AssertionError("fail!")
     }
    """.trimIndent()

val assertkAssertionsStub =
    """
    package assertk.assertions

    fun <T, U> Assert<T>.prop(property: () -> U): Assert<U> {

    }

    fun <T> Assert<T>.isEqualTo(expected: T) {

    }

    fun <T> Assert<T?>.isNotNull(): Assert<T> {

    }

    fun <T> Assert<T>.isTrue() {

    }

    fun <T> Assert<T>.isFalse() {

    }

    fun <T : Any> Assert<Any>.isInstanceOf(clazz: KClass<T>): Assert<T> {
    }
    """.trimIndent()

val assertkCollectionStub =
    """
    package assertk.assertions

    // This name is a hack to get the test infrastructure to correctly
    // name this test stub file's class to AssertkKt
    class Iterable {
    }

    fun <T> Assert<Iterable<T>>.doesNotContain(value: T) {

    }

    fun <T> Assert<Iterable<T>>.contains(value: T) {

    }

    fun <T, U> Assert<Map<T, U>>.key(key: T): Assert<U> {

    }

    fun <T> Assert<Array<T>>.index(index: Int): Assert<T> {

    }
    """.trimIndent()

val ASSERTK_STUBS =
    arrayOf(kotlin(assertkStub), kotlin(assertkAssertionsStub), kotlin(assertkCollectionStub))
