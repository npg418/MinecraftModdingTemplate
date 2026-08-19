package com.npg418.examplemod.config

import com.npg418.examplemod.ExampleMod
import com.npg418.examplemod.config.api.*
import kotlinx.serialization.json.*
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files


class FabricConfigBuilder(private val spec: ConfigSpec) {
    private val path = FabricLoader.getInstance().configDir.resolve("${spec.baseFileName}.json")
    private val json = Json { prettyPrint = true }

    fun register() {
        val existing = readJson()
        val merged = bindSection(spec, existing)
        writeJson(merged)
    }

    private fun bindSection(section: ConfigSection, jsonObject: JsonObject): JsonObject {
        val children = jsonObject.toMutableMap()
        for ((name, node) in section.children) {
            val existing = children[name]
            children[name] = when (node) {
                is RangedConfigEntry<*> -> bindRangedEntry(existing, node)
                is ConfigEntry<*> -> bindEntry(existing, node)
                is ConfigSection -> bindSection(node, existing as? JsonObject ?: JsonObject(emptyMap()))
            }
        }
        return JsonObject(children)
    }

    private fun <T : Comparable<T>> bindRangedEntry(existing: JsonElement?, entry: RangedConfigEntry<T>): JsonElement {
        val parsed = fromJsonElement(entry.default, existing)
        val value = parsed.coerceIn(entry.range)
        if (value != parsed) {
            ExampleMod.LOGGER.warn("Config value {} is out of range. (Range: {}) Value clamped to {}.", entry.name, entry.range, value)
        }
        entry.set { value }
        return toJsonElement(entry)
    }

    private fun <T : Any> bindEntry(
        existing: JsonElement?,
        entry: ConfigEntry<T>
    ): JsonElement {
        val value = fromJsonElement(entry.default, existing)
        entry.set { value }
        return toJsonElement(entry)
    }

    private fun <T : Any> fromJsonElement(default: T, element: JsonElement?): T {
        if (element == null || element !is JsonPrimitive || element is JsonNull) return default

        @Suppress("UNCHECKED_CAST")
        return when (default) {
            is Boolean -> element.boolean
            is Int -> element.int
            is Long -> element.long
            is Double -> element.double
            is Float -> element.float
            is String -> element.content
            is Enum<*> -> default.javaClass.enumConstants.firstOrNull { it.name.equals(element.content, ignoreCase = true) } ?: default
            else -> throw IllegalArgumentException("Unsupported config value type: ${default::class.simpleName}")
        } as T
    }

    private fun toJsonElement(node: ConfigNode): JsonElement = when (node) {
        is ConfigEntry<*> -> node.get().let {
            when (it) {
                is Number -> JsonPrimitive(it)
                is Boolean -> JsonPrimitive(it)
                is String -> JsonPrimitive(it)
                is Enum<*> -> JsonPrimitive(it.name)
                else -> throw IllegalArgumentException("Unsupported config value type: ${this::class.simpleName}")
            }
        }

        is ConfigSection -> JsonObject(node.children.mapValues { (_, childNode) -> toJsonElement(childNode) })
    }


    private fun readJson(): JsonObject = runCatching {
        json.parseToJsonElement(Files.readString(path)).jsonObject
    }.getOrElse {
        ExampleMod.LOGGER.warn("Config file {} was not found or invalid. Falling back default one.", path.fileName)
        JsonObject(emptyMap())
    }

    private fun writeJson(jsonObject: JsonObject) {
        runCatching {
            Files.createDirectories(path.parent)
            Files.writeString(path, json.encodeToString(JsonObject.serializer(), jsonObject))
        }.onFailure {
            ExampleMod.LOGGER.error("Unable to write config file {}.", path.fileName)
        }
    }
}