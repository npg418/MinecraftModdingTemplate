@file:Suppress("SameParameterValue", "Unused")

package com.npg418.examplemod.config.api

import com.npg418.examplemod.ExampleMod
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

open class ConfigEntry<T : Any> internal constructor(val name: String, val default: T) : ConfigNode() {
    open var validator: ((Any) -> Boolean) = { allowedValues?.contains(it) ?: true }
        get() = { value ->
            field(value).also {
                if (!it) ExampleMod.LOGGER.warn(
                    "Config value {} failed validation. Falling back to previous value.",
                    name
                )
            }
        }
    open var allowedValues: Collection<T>? = null
    open var worldRestart = false
    open var gameRestart = false

    @Volatile
    private var source = { default }

    fun get() = source()
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = get()

    fun set(newSource: () -> T) {
        val fallback = source
        source = {
            val value = newSource()
            if (validator(value)) {
                value
            } else {
                fallback()
            }
        }
    }

    fun set(newValue: T) {
        if (validator(newValue)) {
            source = { newValue }
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newSource: () -> T) = set(newSource)
    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) = set(newValue)
}

class RangedConfigEntry<T : Comparable<T>> internal constructor(
    name: String,
    default: T,
    val range: ClosedRange<T>
) : ConfigEntry<T>(name, default) {
    override var validator: ((Any) -> Boolean) = {
        val type = default.javaClass
        type.isInstance(it) && type.cast(it) in range
    }
}

class EnumConfigEntry<T : Enum<T>> internal constructor(name: String, default: T) : ConfigEntry<T>(name, default) {
    override var validator: ((Any) -> Boolean) = { default.declaringJavaClass.isInstance(it) }
}

class ListConfigEntry<T : Any>(
    name: String,
    default: List<T>,
    val newElement: () -> T,
    override var validator: ((Any) -> Boolean)
) :
    ConfigEntry<List<T>>(name, default) {
    var allowEmpty = default.isEmpty()
}

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

    protected fun <T : Any> defineList(
        name: String,
        default: List<T>,
        newElement: () -> T,
        validator: (Any) -> Boolean,
        block: (ListConfigEntry<T>.() -> Unit)? = null
    ) = ListConfigEntry(name, default, newElement, validator).apply { block?.invoke(this) }.also { children[name] = it }

    protected fun <T : ConfigSection> section(name: String, factory: () -> T, block: (T.() -> Unit)? = null) =
        factory().apply { block?.invoke(this) }.also { children[name] = it }
}

abstract class ConfigSpec(val modId: String, val type: ConfigType) : ConfigSection() {
    val baseFileName get() = "$modId-${type.name.lowercase()}"
}