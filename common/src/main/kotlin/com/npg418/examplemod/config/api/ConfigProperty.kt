package com.npg418.examplemod.config.api

import com.mojang.logging.LogUtils
import org.slf4j.Logger
import kotlin.reflect.KProperty

sealed class ConfigProperty<T : Any>(val default: T, val validator: ((Any?) -> Boolean)?) {
    companion object {
        val LOGGER: Logger = LogUtils.getLogger()
    }

    private var localValue = default
    var getter: () -> T = { localValue }
    var setter: (T) -> Unit = {
        if (validator != null && !validator(it)) {
            LOGGER.error("Config value {} is invalid. The value was not set.", it)
        } else {
            localValue = it
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = getter()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        setter(newValue)
    }
}

class NormalConfigProperty<T : Any> internal constructor(default: T, validator: ((Any?) -> Boolean)?) :
    ConfigProperty<T>(default, validator)

class EnumConfigProperty<T : Enum<T>> internal constructor(default: T, validator: ((Any?) -> Boolean)?) :
    ConfigProperty<T>(default, validator)

class ListConfigProperty<E : Any> internal constructor(
    default: List<E>,
    val newElement: () -> E,
    val elementValidator: (Any?) -> Boolean,
    val allowEmpty: Boolean,
    validator: ((Any?) -> Boolean)?
) : ConfigProperty<List<E>>(default, validator)

interface ConstrainedConfigProperty<T : Any> {
    fun validate(value: T): Boolean
    fun invalidMessage(value: T): String
}

class RangedConfigProperty<T : Comparable<T>> internal constructor(
    default: T,
    val range: ClosedRange<T>,
    validator: ((Any?) -> Boolean)?
) : ConfigProperty<T>(default, validator), ConstrainedConfigProperty<T> {
    override fun validate(value: T): Boolean = value in range
    override fun invalidMessage(value: T): String = "$value is not in range $range"
}

class OneOfConfigProperty<T : Any> internal constructor(
    default: T,
    val allowedValues: List<T>,
    validator: ((Any?) -> Boolean)?
) : ConfigProperty<T>(default, validator), ConstrainedConfigProperty<T> {
    override fun validate(value: T): Boolean = value in allowedValues
    override fun invalidMessage(value: T): String = "$value is not one of $allowedValues"
}