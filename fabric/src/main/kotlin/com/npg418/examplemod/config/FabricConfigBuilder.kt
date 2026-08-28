package com.npg418.examplemod.config

import com.mojang.logging.LogUtils
import com.npg418.examplemod.config.api.*
import kotlinx.serialization.json.*
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.safeCast


class FabricConfigBuilder(private val configSpec: ConfigSpec) {
    companion object {
        val LOGGER: Logger = LogUtils.getLogger()
    }

    private val json = Json { prettyPrint = true }

    fun register(modId: String) {
        val configClass = configSpec::class
        val instance = configClass.objectInstance
        if (instance == null) {
            LOGGER.error(
                "Could not register config class {} because it is not object class.",
                configClass.qualifiedName
            )
            return
        }

        val path = FabricLoader.getInstance().configDir.resolve("$modId-${configSpec.configType.name.lowercase()}.json")
        val existing = readJson(path)
        val merged = registerObject(configClass, instance, existing)
        writeJson(path, merged)
    }

    private fun registerObject(kClass: KClass<*>, instance: Any, existing: JsonObject): JsonObject {
        val children = mutableMapOf<String, JsonElement>()
        kClass.declaredMemberProperties.mapNotNull { registerProperty(it, instance, existing) }.forEach(children::plusAssign)

        kClass.nestedClasses.mapNotNull { nestedClass ->
            val nestedInstance = nestedClass.objectInstance
            if (nestedInstance == null) {
                LOGGER.warn(
                    "Nested class {} inside config class {} is not an object. Skipping.",
                    nestedClass.qualifiedName,
                    kClass.qualifiedName
                )
                return@mapNotNull null
            }

            val sectionName = nestedClass.findAnnotation<Name>()?.value ?: nestedClass.simpleName!!
            val nestedExisting = existing[sectionName] as? JsonObject ?: JsonObject(emptyMap())
            children[sectionName] = registerObject(nestedClass, nestedInstance, nestedExisting)
        }
        return JsonObject(children)
    }

    private fun registerProperty(
        property: KProperty1<out Any, *>,
        receiver: Any,
        existing: JsonObject
    ): Pair<String, JsonElement>? {
        val name = property.findAnnotation<Name>()?.value ?: property.name
        property.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val delegate = (property as KProperty1<Any, *>).getDelegate(receiver)
        if (delegate !is ConfigProperty<*>) {
            LOGGER.warn(
                "Property {} in class {} is not a {}. Skipping.",
                name,
                receiver::class.qualifiedName,
                ConfigProperty::class.simpleName
            )
            return null
        }

        val element = existing[name]
        when (delegate) {
            is ListConfigProperty<*> -> if (element is JsonArray) bind(name, delegate, element)
            else -> if (element is JsonPrimitive && element !is JsonNull) {
                when (delegate) {
                    is NormalConfigProperty<*> -> bind(name, delegate, element)
                    is EnumConfigProperty<*> -> bind(name, delegate, element)
                    is RangedConfigProperty<*> -> bind(name, delegate, element)
                    is OneOfConfigProperty<*> -> bind(name, delegate, element)
                }
            }
        }

        return name to toJsonElement(delegate)
    }

    private fun <T : Any> bind(name: String, d: NormalConfigProperty<T>, existing: JsonPrimitive) {
        d.setter(parse(name, d.default, existing))
    }

    private fun <T : Enum<T>> bind(name: String, d: EnumConfigProperty<T>, existing: JsonPrimitive) {
        val enums = d.default.declaringJavaClass.enumConstants
        val content = existing.content
        val found = enums.firstOrNull { it.name.equals(content, ignoreCase = true) }
        if (found == null) {
            LOGGER.warn(
                "Enum config entry {} (value: {}) is not one of [{}]. Falling back to default one ({}).",
                name,
                content,
                enums.joinToString { it.name },
                d.default
            )
        } else {
            d.setter(found)
        }
    }

    private fun <E : Any> bind(name: String, d: ListConfigProperty<E>, existing: JsonArray) {
        if (existing.isEmpty() && !d.allowEmpty) {
            LOGGER.warn("Config entry {} must not be empty. Falling back to default one.", name)
            return
        }
        val sample = d.newElement()
        val result = existing.mapIndexedNotNull { i, elem ->
            val primitive = elem as? JsonPrimitive
            if (primitive == null) {
                LOGGER.warn("Config entry {}[{}] is not primitive value. Skipping.", name, i)
                return@mapIndexedNotNull null
            }
            val parsed = parse("$name[$i]", sample, primitive)
            if (!d.elementValidator(parsed)) {
                LOGGER.warn("Config entry {}[{}] (value: {}) failed validation. Skipping.", name, i, parsed)
                return@mapIndexedNotNull null
            }
            parsed
        }
        if (result.isEmpty() && !d.allowEmpty) {
            LOGGER.warn("Config entry {} became empty after validation. Falling back to default one.", name)
            return
        }
        d.setter(result)
    }

    private fun <T : Comparable<T>> bind(name: String, d: RangedConfigProperty<T>, existing: JsonPrimitive) {
        val parsed = parse(name, d.default, existing)
        val clamped = parsed.coerceIn(d.range)
        if (parsed != clamped) {
            LOGGER.warn(
                "Config entry {} (value: {}) is out of range {}. Clamped to {}.",
                name,
                parsed,
                d.range,
                clamped
            )
        }
        d.setter(clamped)
    }

    private fun <T : Any> bind(name: String, d: OneOfConfigProperty<T>, existing: JsonPrimitive) {
        val parsed = parse(name, d.default, existing)
        if (parsed !in d.allowedValues) {
            LOGGER.warn(
                "Config entry {} (value: {}) is not one of {}. Falling back default one.",
                name,
                parsed,
                d.allowedValues
            )
        } else {
            d.setter(parsed)
        }
    }

    private fun <T : Any> parse(name: String, default: T, existing: JsonElement?): T {
        val primitive = existing as? JsonPrimitive ?: run {
            LOGGER.warn("Config entry {} is not primitive value. Falling back to default one ({}).", name, default)
            return default
        }
        val kClass = default::class
        val parsed = when (kClass) {
            Boolean::class -> primitive.booleanOrNull
            Int::class -> primitive.intOrNull
            Long::class -> primitive.longOrNull
            Double::class -> primitive.doubleOrNull
            Float::class -> primitive.floatOrNull
            String::class -> primitive.contentOrNull
            else -> throw IllegalArgumentException("Unsupported config value type: ${kClass.qualifiedName}")
        }
        return kClass.safeCast(parsed) ?: run {
            LOGGER.warn(
                "Config entry {} (value: {}) is an invalid representation. Falling back to default one ({}).",
                name,
                parsed,
                default
            )
            default
        }
    }

    private fun <T : Any> toJsonElement(property: ConfigProperty<T>): JsonElement {
        return when(property) {
            is ListConfigProperty<*> -> JsonArray(property.getter().map(::toJsonPrimitive))
            else -> toJsonPrimitive(property.getter())
        }
    }

    private fun toJsonPrimitive(value: Any): JsonPrimitive = when (value) {
        is Number -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is String -> JsonPrimitive(value)
        is Enum<*> -> JsonPrimitive(value.name)
        else -> throw IllegalArgumentException("Unsupported config value type: ${value::class.simpleName}")
    }

    private fun readJson(path: Path): JsonObject = runCatching {
        json.parseToJsonElement(Files.readString(path)).jsonObject
    }.getOrElse {
        LOGGER.warn("Config file {} was not found or invalid. Falling back default one.", path.fileName)
        JsonObject(emptyMap())
    }

    private fun writeJson(path: Path, jsonObject: JsonObject) {
        runCatching {
            Files.createDirectories(path.parent)
            Files.writeString(path, json.encodeToString(JsonObject.serializer(), jsonObject))
        }.onFailure {
            LOGGER.error("Unable to write config file {}.", path.fileName)
        }
    }
}