package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin

val assertkStub =
    """
    @file:JvmName("AssertKt")
    package assertk

    import java.lang.AssertionError

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
    """.trimIndent()

val assertkIterableStub =
    """
    @file:JvmName("IterableKt")
    package assertk.assertions

    fun <T> Assert<Iterable<T>>.doesNotContain(value: T) {

    }

    fun <T> Assert<Iterable<T>>.contains(value: T) {

    }

    fun <T, U> Assert<Map<T, U>>.key(key: T): Assert<U> {

    }

    fun <T> Assert<Array<T>>.index(index: Int): Assert<T> {

    }
    """.trimIndent()

val assertkCollectionStub =
    """
    @file:JvmName("CollectionKt")
    package assertk.assertions

    fun Assert<Collection<*>>.hasSize(value: Int) {

    }
    """.trimIndent()

val assertkAnyStub =
    """
    @file:JvmName("AnyKt")
    package assertk.assertions

    inline fun <reified T : Any> Assert<Any?>.isInstanceOf(): Assert<T> = isInstanceOf(T::class)

    fun <T : Any> Assert<Any?>.isInstanceOf(clazz: KClass<T>): Assert<T> {
    }
    """.trimIndent()

val assertkAnyJvmStub =
    """
    @file:JvmName("AnyJVMKt")
    package assertk.assertions

    fun <T : Any> Assert<Any?>.isInstanceOf(clazz: Class<T>): Assert<T> {
    }
    """.trimIndent()

val ASSERTK_STUBS =
    arrayOf(
        kotlin(assertkStub),
        kotlin(assertkAssertionsStub),
        kotlin(assertkCollectionStub),
        kotlin(assertkIterableStub),
        kotlin(assertkAnyStub),
        kotlin(assertkAnyJvmStub),
    )
