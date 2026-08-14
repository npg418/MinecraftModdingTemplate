package com.npg418.examplemod.config.api

import kotlin.reflect.KProperty

enum class ConfigType {
    COMMON,
    CLIENT,
    SERVER,
    STARTUP
}

sealed class ConfigNode {
    open var comment: String? = null
    open var translation: String? = null
}

open class ConfigEntry<T : Any>(val default: T) : ConfigNode() {
    @Volatile
    var source = { default }

    fun get() = source()
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = get()
}

class RangedConfigEntry<T : Comparable<T>>(default: T, val range: ClosedRange<T>) : ConfigEntry<T>(default)

open class ConfigSection internal constructor(private val name: String) : ConfigNode() {
    val children = linkedMapOf<String, ConfigNode>()

    protected fun <T : Any> define(
        name: String,
        default: T,
        block: (ConfigEntry<T>.() -> Unit)? = null
    ) = ConfigEntry(default).apply { block?.invoke(this) }.also {
        children[name] = it
    }

    protected fun <T : Comparable<T>> defineInRange(
        name: String,
        default: T,
        range: ClosedRange<T>,
        block: (RangedConfigEntry<T>.() -> Unit)? = null
    ) = RangedConfigEntry(default, range).apply { block?.invoke(this) }.also {
        children[name] = it
    }

    protected fun <S : ConfigSection> section(constructor: () -> S) = constructor().also {
        children[it.name] = it
    }
}

abstract class ConfigSpec(val modId: String, val type: ConfigType) : ConfigSection("") {
    val baseFileName get() = "$modId-${type.name.lowercase()}"
}