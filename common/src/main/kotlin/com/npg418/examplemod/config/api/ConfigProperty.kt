package com.npg418.examplemod.config.api

import kotlin.reflect.KProperty

sealed class ConfigProperty<T : Any>(val default: T) {
    protected open var localValue = default
    var getter: () -> T = { localValue }
    var setter: (T) -> Unit = { localValue = it }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = getter()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        setter(newValue)
    }
}

class NormalConfigProperty<T : Any> internal constructor(default: T) : ConfigProperty<T>(default)

class EnumConfigProperty<T : Enum<T>> internal constructor(default: T) : ConfigProperty<T>(default)

interface ConstrainedConfigProperty<T : Any> {
    fun validate(value: T): Boolean
    fun invalidMessage(value: T): String
}

class RangedConfigProperty<T : Comparable<T>> internal constructor(default: T, val range: ClosedRange<T>) :
    ConfigProperty<T>(default), ConstrainedConfigProperty<T> {
    override fun validate(value: T): Boolean = value in range
    override fun invalidMessage(value: T): String = "$value is not in range $range"
}

class ListedConfigProperty<T : Any> internal constructor(default: T, val allowedValues: List<T>) :
    ConfigProperty<T>(default), ConstrainedConfigProperty<T> {
    override fun validate(value: T): Boolean = value in allowedValues
    override fun invalidMessage(value: T): String = "$value is not one of $allowedValues"
}