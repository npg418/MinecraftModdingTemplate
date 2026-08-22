package com.npg418.examplemod.config

import com.npg418.examplemod.config.api.*
import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.ModConfigSpec

class NeoForgeConfigBuilder(private val spec: ConfigSpec) {
    private val builder = ModConfigSpec.Builder()

    fun register(container: ModContainer) {
        bindSection(spec)
        container.registerConfig(ModConfig.Type.valueOf(spec.type.name), builder.build(), "${spec.baseFileName}.toml")
    }

    private fun bindSection(section: ConfigSection) {
        for ((name, node) in section.children) {
            node.comment?.let(builder::comment)
            node.translation?.let(builder::translation)

            if (node is ConfigEntry<*>) {
                if (node.worldRestart) builder.worldRestart()
                if (node.gameRestart) builder.gameRestart()
            }

            when (node) {
                is RangedConfigEntry<*> -> bindRangedEntry(name, node)
                is EnumConfigEntry<*> -> bindEnumEntry(name, node)
                is ListConfigEntry<*> -> bindListEntry(name, node)
                is ConfigEntry<*> -> bindEntry(name, node)
                is ConfigSection -> {
                    builder.push(name)
                    try {
                        bindSection(node)
                    } finally {
                        builder.pop()
                    }
                }
            }
        }
    }

    private fun <T : Any> bindEntry(name: String, entry: ConfigEntry<T>) {
        val allowed = entry.allowedValues
        if (allowed != null) {
            builder.defineInList(name, entry.default, allowed)
        } else {
            builder.define(name, entry.default)
        }.let { entry.set(it::get) }
    }

    private fun <T : Comparable<T>> bindRangedEntry(name: String, entry: RangedConfigEntry<T>) {
        builder.defineInRange(
            name,
            entry.default,
            entry.range.start,
            entry.range.endInclusive,
            entry.default.javaClass
        ).let { entry.set(it::get) }
    }

    private fun <T : Enum<T>> bindEnumEntry(name: String, entry: EnumConfigEntry<T>) {
        builder.defineEnum(name, entry.default).let { entry.set(it::get) }
    }

    private fun <T : Any> bindListEntry(name: String, entry: ListConfigEntry<T>) {
        if (entry.allowEmpty) {
            builder.defineListAllowEmpty(name, entry.default, entry.newElement, entry.validator)
        } else {
            builder.defineList(name, entry.default, entry.newElement, entry.validator)
        }.let { entry.set(it::get) }
    }
}