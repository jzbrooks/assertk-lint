package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin

val assertkStub =
    """
    package assertk

    class Assert<T> {
        override fun equals(other: Any?): Boolean {

        }
    }

    fun <T> assertThat(subject: T?): Assert<T> {

    }

    fun <T> Assert<T>.all(body: Assert<T>.() -> Unit) {

    }

    fun fail() {
        throw java.lang.AssertionError("fail!")
    }
    """.trimIndent()

val assertkAssertionsStub =
    """
    @file:JvmName("AnyKt")
    package assertk.assertions

    fun <T, U> assertk.Assert<T>.prop(property: () -> U): Assert<U> {

    }

    fun <T> assertk.Assert<T>.isEqualTo(expected: T) {

    }

    fun <T> assertk.Assert<T?>.isNotNull(): Assert<T> {

    }

    fun <T> assertk.Assert<T>.isTrue() {

    }

    fun <T> assertk.Assert<T>.isFalse() {

    }

    inline fun <reified T : Any> assertk.Assert<Any?>.isInstanceOf(): Assert<T> = isInstanceOf(T::class)

    fun <T : Any> assertk.Assert<Any?>.isInstanceOf(clazz: KClass<T>): Assert<T> {
    }
    """.trimIndent()

val assertkIterableStub =
    """
    @file:JvmName("IterableKt")
    package assertk.assertions

    fun <T> assertk.Assert<Iterable<T>>.doesNotContain(value: T) {

    }

    fun <T> assertk.Assert<Iterable<T>>.contains(value: T) {

    }

    fun <T, U> assertk.Assert<Map<T, U>>.key(key: T): Assert<U> {

    }

    fun <T> assertk.Assert<Array<T>>.index(index: Int): Assert<T> {

    }

    fun <T> Assert<List<T>>.index(index: Int): Assert<T> {

    }

    fun <T> Assert<Iterable<T>>.first(): Assert<T> {

    }

    fun <T> Assert<Iterable<T>>.single(): Assert<T> {

    }
    """.trimIndent()

val assertkCollectionStub =
    """
    @file:JvmName("CollectionKt")
    package assertk.assertions

    fun Assert<Collection<*>>.hasSize(value: Int) {

    }
    """.trimIndent()

val assertkComparableStub =
    """
    @file:JvmName("ComparableKt")
    package assertk.assertions

    fun <A, B : Comparable<A>> assertk.Assert<B>.isGreaterThan(other: A) {

    }
    """.trimIndent()

val assertkAnyJvmStub =
    """
    @file:JvmName("AnyJVMKt")
    package assertk.assertions

    fun <T : Any> assertk.Assert<Any?>.isInstanceOf(clazz: Class<T>): Assert<T> {
    }
    """.trimIndent()

val ASSERTK_STUBS =
    arrayOf(
        kotlin(assertkStub),
        kotlin(assertkAssertionsStub),
        kotlin(assertkCollectionStub),
        kotlin(assertkComparableStub),
        kotlin(assertkIterableStub),
        kotlin(assertkAnyJvmStub),
    )
