package com.npg418.examplemod.config

import com.npg418.examplemod.ExampleMod
import com.npg418.examplemod.config.api.ConfigEntry
import com.npg418.examplemod.config.api.ConfigNode
import com.npg418.examplemod.config.api.ConfigSection
import com.npg418.examplemod.config.api.ConfigSpec
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
            when (node) {
                is ConfigEntry<*> -> {
                    if (existing is JsonPrimitive) {
                        node.set { fromJsonElement(node.default, existing) }
                    } else {
                        children[name] = toJsonElement(node)
                    }
                }

                is ConfigSection -> children[name] =
                    bindSection(node, existing as? JsonObject ?: JsonObject(emptyMap()))
            }
        }
        return JsonObject(children)
    }

    private fun <T : Any> fromJsonElement(default: T, primitive: JsonPrimitive): T {
        if (primitive is JsonNull) return default

        @Suppress("UNCHECKED_CAST")
        return when (default) {
            is Boolean -> primitive.boolean
            is Int -> primitive.int
            is Long -> primitive.long
            is Double -> primitive.double
            is Float -> primitive.float
            is String -> primitive.content
            is Enum<*> -> default.javaClass.enumConstants.firstOrNull { it.name == primitive.content } ?: default
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