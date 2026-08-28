package com.npg418.examplemod.config.api

@Suppress("Unused", "SameParameterValue")
abstract class ConfigSpec(val configType: ConfigType) {
    protected fun <T : Any> defining(default: T, validator: ((Any?) -> Boolean)? = null) =
        NormalConfigProperty(default, validator)

    protected fun <T : Enum<T>> definingEnum(default: T, validator: ((Any?) -> Boolean)? = null) =
        EnumConfigProperty(default, validator)

    protected fun <T : Comparable<T>> definingInRange(
        default: T,
        range: ClosedRange<T>,
        validator: ((Any?) -> Boolean)? = null
    ) = RangedConfigProperty(default, range, validator)

    protected fun <T : Any> definingInList(default: T, allowedValues: List<T>, validator: ((Any?) -> Boolean)? = null) =
        OneOfConfigProperty(default, allowedValues, validator)

    protected fun <E : Any> definingList(
        default: List<E>,
        newElement: () -> E,
        elementValidator: (Any?) -> Boolean,
        allowEmpty: Boolean = default.isEmpty(),
        validator: ((Any?) -> Boolean)? = null
    ) = ListConfigProperty(default, newElement, elementValidator, allowEmpty, validator)
}