package com.npg418.examplemod.config.api

import kotlin.reflect.KProperty

enum class ConfigType {
    COMMON,
    CLIENT,
    SERVER,
    STARTUP
}

sealed class ConfigNode {
    var comment: String? = null
    var translation: String? = null
}

open class ConfigEntry<T : Any> internal constructor(val name: String, val default: T) : ConfigNode() {
    @Volatile
    private var source = { default }

    fun get() = source()
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = get()

    fun set(newSource: () -> T) {
        source = newSource
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newSource: () -> T) = set(newSource)
}

class RangedConfigEntry<T : Comparable<T>> internal constructor(
    name: String,
    default: T,
    val range: ClosedRange<T>
) : ConfigEntry<T>(name, default)

class EnumConfigEntry<T : Enum<T>> internal constructor(name: String, default: T) : ConfigEntry<T>(name, default)

abstract class ConfigSection : ConfigNode() {
    val children = linkedMapOf<String, ConfigNode>()

    protected fun <T : Any> define(name: String, default: T, block: (ConfigEntry<T>.() -> Unit)? = null) =
        ConfigEntry(name, default).apply { block?.invoke(this) }.also { children[name] = it }

    protected fun <T : Comparable<T>> defineInRange(
        name: String,
        default: T,
        range: ClosedRange<T>,
        block: (ConfigEntry<T>.() -> Unit)? = null
    ) = RangedConfigEntry(name, default, range).apply { block?.invoke(this) }.also { children[name] = it }

    protected fun <T : Enum<T>> defineEnum(name: String, default: T, block: (EnumConfigEntry<T>.() -> Unit)? = null) =
        EnumConfigEntry(name, default).apply { block?.invoke(this) }.also { children[name] = it }

    protected fun <T : ConfigSection> section(name: String, factory: () -> T, block: (T.() -> Unit)? = null) =
        factory().apply { block?.invoke(this) }.also { children[name] = it }
}

abstract class ConfigSpec(val modId: String, val type: ConfigType) : ConfigSection() {
    val baseFileName get() = "$modId-${type.name.lowercase()}"
}