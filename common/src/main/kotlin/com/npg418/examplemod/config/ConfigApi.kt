package com.npg418.examplemod.config

import kotlin.reflect.KProperty

interface ConfigBinder {
    fun <T : Any> source(entry: ConfigEntry<T>): () -> T
    fun <T : Comparable<T>> source(entry: RangedConfigEntry<T>): () -> T
}

open class ConfigEntry<T : Any> internal constructor(
    val path: List<String>,
    val default: T,
    val comment: String? = null
) {
    @Volatile
    private var source: () -> T = { default }

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        set(newValue)
    }

    fun get() = source()
    fun set(newValue: T) {
        source = { newValue }
    }

    fun bindTo(binder: ConfigBinder) {
        source = binder.source(this)
    }

    internal fun bindTo(entry: ConfigEntry<T>) {
        source = entry::get
    }
}

class RangedConfigEntry<T : Comparable<T>> internal constructor(
    path: List<String>,
    default: T,
    val range: ClosedRange<T>,
    comment: String? = null
) : ConfigEntry<T>(path, default, comment) {
    operator fun contains(other: T) = other in range
}

enum class ConfigType {
    COMMON,
    CLIENT,
    SERVER,
    STARTUP
}

sealed interface ConfigContainer {
    val path: List<String>
    val root: ConfigSpec

    fun <T : Any> define(key: String, default: T, comment: String? = null): ConfigEntry<T> =
        ConfigEntry(path + key, default, comment).also(root.configEntries::add)

    fun <T : Comparable<T>> defineInRange(
        key: String,
        default: T,
        range: ClosedRange<T>,
        comment: String? = null
    ): RangedConfigEntry<T> {
        require(default in range) {
            "Default value $default of ${(path + key).joinToString(".")} is out of range $range"
        }
        return RangedConfigEntry(path + key, default, range, comment).also(root.configEntries::add)
    }
}

abstract class ConfigSpec(val modId: String, val type: ConfigType) : ConfigContainer {
    internal val configEntries = mutableListOf<ConfigEntry<*>>()
    val entries: List<ConfigEntry<*>> get() = configEntries.toList()

    val baseFileName: String get() = "$modId-${type.name.lowercase()}"

    override val path: List<String> = emptyList()
    override val root: ConfigSpec get() = this

    fun bind(binder: ConfigBinder) = entries.forEach { it.bindTo(binder) }
}

abstract class ConfigSection(
    val parent: ConfigContainer,
    val name: String
) : ConfigContainer {
    override val path: List<String> = parent.path + name
    override val root: ConfigSpec get() = parent.root
}

object ConfigRegistry {
    private val registeredConfigs = mutableListOf<ConfigSpec>()

    val configs: List<ConfigSpec>
        get() = registeredConfigs.toList()

    fun register(config: ConfigSpec) {
        registeredConfigs.add(config)
    }

    fun registerAll(vararg configs: ConfigSpec) {
        configs.forEach(::register)
    }

    fun mergedEntries(type: ConfigType): List<ConfigEntry<*>> {
        val entriesByPath = linkedMapOf<List<String>, ConfigEntry<*>>()

        registeredConfigs
            .asSequence()
            .filter { it.type == type }
            .flatMap { it.entries.asSequence() }
            .forEach { entry ->
                entriesByPath[entry.path] = entry
            }

        return entriesByPath.values.toList()
    }

    fun bindOverriddenEntries(type: ConfigType) {
        val winningEntriesByPath = mergedEntries(type).associateBy { it.path }

        registeredConfigs
            .asSequence()
            .filter { it.type == type }
            .flatMap { it.entries.asSequence() }
            .forEach { entry ->
                val winningEntry = winningEntriesByPath[entry.path]
                if (winningEntry != null && winningEntry !== entry) {
                    bindEntryToWinningEntry(entry, winningEntry)
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> bindEntryToWinningEntry(entry: ConfigEntry<*>, winningEntry: ConfigEntry<T>) {
        (entry as ConfigEntry<T>).bindTo(winningEntry)
    }
}