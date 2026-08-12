package com.npg418.examplemod.config

import com.npg418.examplemod.config.ConfigType.*
import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.ModConfigSpec

fun ConfigType.toModConfigType(): ModConfig.Type = when (this) {
    COMMON -> ModConfig.Type.COMMON
    CLIENT -> ModConfig.Type.CLIENT
    SERVER -> ModConfig.Type.SERVER
}

private class NeoForgeConfigBinder(private val builder: ModConfigSpec.Builder) : ConfigBinder {
    override fun <T : Any> source(entry: ConfigEntry<T>): () -> T {
        entry.comment?.let(builder::comment)
        return builder.define(entry.path, entry.default)::get
    }

    override fun <T : Comparable<T>> source(entry: RangedConfigEntry<T>): () -> T {
        entry.comment?.let(builder::comment)
        return builder.defineInRange(
            entry.path, entry.default, entry.range.start, entry.range.endInclusive, entry.default.javaClass,
        )::get
    }
}

fun registerConfigs(container: ModContainer) {
    ConfigRegistry.all.forEach { spec ->
        val builder = ModConfigSpec.Builder()
        spec.bind(NeoForgeConfigBinder(builder))
        container.registerConfig(spec.type.toModConfigType(), builder.build(), "${spec.fileName}.toml")
    }
}