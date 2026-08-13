package com.npg418.examplemod.config

import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.ModConfigSpec

fun ConfigType.toModConfigType(): ModConfig.Type = when (this) {
    ConfigType.COMMON -> ModConfig.Type.COMMON
    ConfigType.CLIENT -> ModConfig.Type.CLIENT
    ConfigType.SERVER -> ModConfig.Type.SERVER
    ConfigType.STARTUP -> ModConfig.Type.STARTUP
}

private class NeoForgeConfigBinder(private val builder: ModConfigSpec.Builder) : ConfigBinder {
    override fun <T : Any> source(entry: ConfigEntry<T>): () -> T {
        entry.comment?.let(builder::comment)
        return builder.define(entry.path, entry.default)::get
    }

    override fun <T : Comparable<T>> source(entry: RangedConfigEntry<T>): () -> T {
        entry.comment?.let(builder::comment)
        return builder.defineInRange(
            entry.path,
            entry.default,
            entry.range.start,
            entry.range.endInclusive,
            entry.default.javaClass,
        )::get
    }
}

fun registerConfigs(container: ModContainer) {
    ConfigType.entries.forEach { type ->
        val entries = ConfigRegistry.mergedEntries(type)
        if (entries.isEmpty()) {
            return@forEach
        }

        val builder = ModConfigSpec.Builder()
        val binder = NeoForgeConfigBinder(builder)

        entries.forEach { it.bindTo(binder) }
        ConfigRegistry.bindOverriddenEntries(type)

        val modId = ConfigRegistry.configs.first { it.type == type }.modId

        container.registerConfig(
            type.toModConfigType(),
            builder.build(),
            "$modId-${type.name.lowercase()}.toml"
        )
    }
}