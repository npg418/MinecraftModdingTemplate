package com.npg418.examplemod.config

import com.npg418.examplemod.config.api.*
import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.ModConfigSpec

fun ConfigType.toModConfigType(): ModConfig.Type = when (this) {
    ConfigType.COMMON -> ModConfig.Type.COMMON
    ConfigType.CLIENT -> ModConfig.Type.CLIENT
    ConfigType.SERVER -> ModConfig.Type.SERVER
    ConfigType.STARTUP -> ModConfig.Type.STARTUP
}

class NeoForgeConfigBuilder(private val spec: ConfigSpec) {
    private val builder = ModConfigSpec.Builder()

    fun register(container: ModContainer) {
        bindSection(spec)
        container.registerConfig(spec.type.toModConfigType(), builder.build(), "${spec.baseFileName}.toml")
    }

    private fun <T : Any> bindEntry(name: String, entry: ConfigEntry<T>) {
        entry.set(builder.define(name, entry.default)::get)
    }

    private fun  bindRangedEntry(name: String, entry: RangedConfigEntry<*>) {
        entry.set(
            builder.defineInRange(
                name,
                entry.default,
                entry.range.start,
                entry.range.endInclusive,
                entry.default.javaClass
            )::get
        )
    }

    private fun bindEnumEntry(name: String, entry: EnumConfigEntry<*>) {
        entry.set(builder.defineEnum(name, entry.default)::get)
    }


    private fun bindSection(section: ConfigSection) {
        for ((name, node) in section.children) {
            node.comment?.let(builder::comment)
            node.translation?.let(builder::translation)
            when (node) {
                is RangedConfigEntry<*> -> bindRangedEntry(name, node)
                is EnumConfigEntry<*> -> bindEnumEntry(name, node)
                is ConfigEntry<*> -> bindEntry(name, node)
                is ConfigSection -> {
                    builder.push(name)
                    bindSection(node)
                    builder.pop()
                }
            }
        }
    }
}