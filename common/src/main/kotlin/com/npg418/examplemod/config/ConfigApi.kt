package com.npg418.examplemod.config

import kotlin.reflect.KProperty

interface ConfigBinder {
    fun <T : Any> source(entry: ConfigEntry<T>): () -> T
    fun <T : Comparable<T>> source(entry: RangedConfigEntry<T>): () -> T
}

open class ConfigEntry<T : Any> internal constructor(
    val path: List<String>,
    val default: T,
    val comment: String? = null,
) {
    @Volatile
    private var source: () -> T = { default }

    fun get(): T = source()
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = source()

    internal fun bind(source: () -> T) {
        this.source = source
    }

    internal open fun bindTo(binder: ConfigBinder) = bind(binder.source(this))

}

class RangedConfigEntry<T> internal constructor(
    path: List<String>,
    default: T,
    comment: String?,
    val range: ClosedRange<T>,
) : ConfigEntry<T>(path, default, comment) where T : Any, T : Comparable<T> {
    operator fun contains(value: T): Boolean = value in range

    fun coerce(value: T): T = value.coerceIn(range)

    override fun bindTo(binder: ConfigBinder) = bind(binder.source(this))
}

enum class ConfigType(val fileNamePrefix: String) { COMMON("common"), CLIENT("client"), SERVER("server") }

abstract class ConfigSpec(val modId: String, val type: ConfigType) {
    private val _entries = mutableListOf<ConfigEntry<*>>()
    val entries: List<ConfigEntry<*>> get() = _entries
    private var path = emptyList<String>()

    val fileName: String get() = "${type.fileNamePrefix}-$modId"

    protected fun <T : Any> define(key: String, default: T, comment: String? = null) =
        ConfigEntry(path + key, default, comment).also { _entries += it }

    protected fun <T> defineInRange(
        key: String,
        default: T,
        range: ClosedRange<T>,
        comment: String? = null,
    ): RangedConfigEntry<T> where T : Any, T : Comparable<T> {
        require(default in range) {
            "Default value $default of ${(path + key).joinToString(".")} is out of range $range"
        }
        return RangedConfigEntry(path + key, default, comment, range).also { _entries += it }
    }

    protected fun section(name: String, block: () -> Unit) {
        path = path + name
        try {
            block()
        } finally {
            path = path.dropLast(1)
        }
    }

    fun bind(binder: ConfigBinder) = entries.forEach { it.bindTo(binder) }
}

object ConfigRegistry {
    private val specs = linkedMapOf<String, ConfigSpec>()
    val all: Collection<ConfigSpec> get() = specs.values

    fun register(spec: ConfigSpec) {
        specs[spec.fileName] = spec
    }
}