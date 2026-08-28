package com.npg418.examplemod.config

import com.npg418.examplemod.ExampleMod
import com.npg418.examplemod.config.api.*
import kotlinx.serialization.json.*
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files


class FabricConfigBuilder(private val spec: Config) {
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
                is RangedConfigEntry<*> -> bindRangedEntry(node, existing as? JsonPrimitive ?: JsonNull)
                is ListConfigEntry<*> -> bindListEntry(node, existing as? JsonArray)
                is ConfigProperty<*> -> bindEntry(node, existing as? JsonPrimitive ?: JsonNull)
                is ConfigSection -> bindSection(node, existing as? JsonObject ?: JsonObject(emptyMap()))
            }
        }
        return JsonObject(children)
    }

    private fun <T : Any> bindEntry(
        entry: ConfigProperty<T>,
        existing: JsonPrimitive,
    ): JsonElement {
        val value = fromJsonPrimitive(entry.default, existing)
        entry.set(value)
        return toJsonElement(entry)
    }

    private fun <T : Comparable<T>> bindRangedEntry(entry: RangedConfigEntry<T>, existing: JsonPrimitive): JsonElement {
        val parsed = fromJsonPrimitive(entry.default, existing)
        val value = parsed.coerceIn(entry.range)
        if (value != parsed) {
            ExampleMod.LOGGER.warn(
                "Config value {} is out of range. (Value: {}, Range: {}) Value clamped to {}.",
                entry.name,
                parsed,
                entry.range,
                value
            )
        }
        entry.set(value)
        return toJsonElement(entry)
    }

    private fun <T : Any> bindListEntry(entry: ListConfigEntry<T>, existing: JsonArray?): JsonElement {
        if (existing != null) {
            val sample = entry.newElement()
            val parsed = existing.filterIsInstance<JsonPrimitive>().map { fromJsonPrimitive(sample, it) }
            if (parsed.isNotEmpty() || entry.allowEmpty) entry.set(parsed)
        }
        return toJsonElement(entry)
    }

    private fun <T : Any> fromJsonPrimitive(default: T, primitive: JsonPrimitive): T {
        if (primitive is JsonNull) return default

        @Suppress("UNCHECKED_CAST")
        return when (default) {
            is Boolean -> primitive.boolean
            is Int -> primitive.int
            is Long -> primitive.long
            is Double -> primitive.double
            is Float -> primitive.float
            is String -> primitive.content
            is Enum<*> -> default.declaringJavaClass.enumConstants.firstOrNull {
                it.name.equals(
                    primitive.content,
                    ignoreCase = true
                )
            } ?: default

            else -> throw IllegalArgumentException("Unsupported config value type: ${default::class.simpleName}")
        } as T
    }

    private fun toJsonElement(node: ConfigNode): JsonElement = when (node) {
        is ListConfigEntry<*> -> JsonArray(node.get().map(::toJsonPrimitive))
        is ConfigProperty<*> -> node.get().let(::toJsonPrimitive)
        is ConfigSection -> JsonObject(node.children.mapValues { (_, childNode) -> toJsonElement(childNode) })
    }

    private fun toJsonPrimitive(value: Any): JsonPrimitive = when (value) {
        is Number -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is String -> JsonPrimitive(value)
        is Enum<*> -> JsonPrimitive(value.name)
        else -> throw IllegalArgumentException("Unsupported config value type: ${this::class.simpleName}")
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