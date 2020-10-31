package com.hexagonkt.injection

import kotlin.reflect.KClass

data class Target<out T : Any>( // Or Definition, Type (used by Kotlin)
    val type: KClass<out T>,
    val tag: Any? = null
)

data class Binding<out T : Any>(
    val type: KClass<out T>,
    val provider: Provider<T>,
    val tag: Any? = null
) {

    constructor(type: KClass<T>, tag: Any? = null, generator: () -> T) :
        this(type, Generator(generator), tag)

    constructor(type: KClass<T>, instance: T, tag: Any? = null) :
        this(type, Instance(instance), tag)

    fun inject(): T =
        provider.provide()
}

fun <T:Any> binding(type: KClass<T>, tag: Any? = null, generator: () -> T): Binding<T> =
    Binding(type, Generator(generator), tag)

fun <T:Any> binding(type: KClass<T>, instance: T, tag: Any? = null): Binding<T> =
    Binding(type, Instance(instance), tag)

inline fun <reified T:Any> binding(tag: Any? = null, noinline generator: () -> T): Binding<T> =
    Binding(T::class, Generator(generator), tag)

inline fun <reified T:Any> binding(instance: T, tag: Any? = null): Binding<T> =
    Binding(T::class, Instance(instance), tag)

fun <T:Any> bindings(type: KClass<T>, vararg generators: Provider<T>): List<Binding<T>> =
    generators
        .mapIndexed { ii, it -> Binding(type, it, ii) }

fun <T:Any> bindingSet(type: KClass<T>, vararg generators: Provider<T>): List<Binding<T>> =
    generators
        .mapIndexed { ii, it -> Binding(type, it, ii) }

fun <T:Any> bindingSet(type: KClass<T>, vararg instances: T): List<Binding<T>> =
    instances
        .mapIndexed { ii, it -> Binding(type, Instance(it), ii) }

fun <T:Any> bindingSet(type: KClass<T>, vararg generators: Pair<Any, Provider<T>>): List<Binding<T>> =
    generators
        .map { (k, v) -> Binding(type, v, k) }

sealed class Provider<out T : Any> {
    abstract fun provide(): T
}

data class Generator<out T : Any>(val generator: () -> T) : Provider<T>() {
    override fun provide(): T =
        generator()
}

data class Instance<out T : Any>(val instance: T) : Provider<T>() {
    override fun provide(): T =
        instance
}

data class Injector(
    val bindings: List<Binding<Any>>
) {

    private val bindingsMap: Map<Pair<KClass<out Any>, Any?>, Binding<Any>> = bindings
        .map { (it.type to it.tag) to it  }
        .toMap()

    constructor(vararg bindings: Binding<Any>) :
        this(bindings.toList())
}

fun t() {
    Binding(String::class, Instance("str"), 0)
    Binding(String::class, Generator { "str" }, 0)
    Injector(
        *bindingSet(CharSequence::class, "a", "b", "c").toTypedArray(),
        binding<CharSequence>("str", "tag"),
        binding<CharSequence>(Unit) { "str" },
        binding<CharSequence>("str"),
        binding<CharSequence> { "str" }
    )
}

fun dcc() {
    class Config(val m: Map<String, Any?>) {
        val s: String by m
    }

    assert("foo" == Config(mapOf("s" to "foo")).s)
}
