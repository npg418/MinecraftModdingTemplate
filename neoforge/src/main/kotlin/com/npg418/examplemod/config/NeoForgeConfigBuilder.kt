package com.npg418.examplemod.config

import com.mojang.logging.LogUtils
import com.npg418.examplemod.config.api.*
import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.ModConfigSpec
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

class NeoForgeConfigBuilder(val configSpec: ConfigSpec) {
    companion object {
        private val LOGGER = LogUtils.getLogger()
    }

    private val builder = ModConfigSpec.Builder()

    fun register(container: ModContainer) {
        val configClass = configSpec::class
        val instance = configClass.objectInstance
        if (instance == null) {
            LOGGER.error(
                "Could not register config class {} because it is not object class.",
                configClass.qualifiedName
            )
            return
        }
        registerObject(configClass, instance)
        container.registerConfig(ModConfig.Type.valueOf(configSpec.configType.name), builder.build())
    }

    private fun applyCommentAndTranslation(annotated: KAnnotatedElement) {
        annotated.findAnnotation<Comment>()?.let { builder.comment(it.value) }
        annotated.findAnnotation<Translation>()?.let { builder.translation(it.value) }
    }

    private fun registerObject(kClass: KClass<*>, instance: Any) {
        kClass.declaredMemberProperties.forEach { registerProperty(it, instance) }

        kClass.nestedClasses.forEach { nestedClass ->
            val nestedInstance = nestedClass.objectInstance
            if (nestedInstance == null) {
                LOGGER.warn(
                    "Nested class {} inside config class {} is not an object. Skipping.",
                    nestedClass.qualifiedName,
                    kClass.qualifiedName
                )
                return@forEach
            }

            val sectionName = nestedClass.findAnnotation<Name>()?.value ?: nestedClass.simpleName!!
            applyCommentAndTranslation(nestedClass)
            builder.push(sectionName)
            try {
                registerObject(nestedClass, nestedInstance)
            } finally {
                builder.pop()
            }
        }
    }

    private fun registerProperty(property: KProperty1<out Any, *>, receiver: Any) {
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
            return
        }

        applyCommentAndTranslation(property)
        property.findAnnotation<WorldRestart>()?.let { builder.worldRestart() }
        property.findAnnotation<GameRestart>()?.let { builder.gameRestart() }

        when (delegate) {
            is NormalConfigProperty<*> -> bind(name, delegate)
            is EnumConfigProperty<*> -> bind(name, delegate)
            is ListConfigProperty<*> -> bind(name, delegate)
            is RangedConfigProperty<*> -> bind(name, delegate)
            is OneOfConfigProperty<*> -> bind(name, delegate)
        }
    }

    private fun <T : Any> ModConfigSpec.ConfigValue<T>.bind(d: ConfigProperty<T>) {
        d.getter = this::get
        d.setter = this::set
    }

    private fun <T : Any> bind(name: String, d: NormalConfigProperty<T>) {
        val configValue = d.validator?.let {
            builder.define(name, d.default, it)
        } ?: builder.define(name, d.default)
        configValue.bind(d)
    }

    private fun <T : Enum<T>> bind(name: String, d: EnumConfigProperty<T>) {
        val configValue = d.validator?.let {
            builder.defineEnum(name, d.default, it)
        } ?: builder.defineEnum(name, d.default)
        configValue.bind(d)
    }

    private fun <E : Any> bind(name: String, d: ListConfigProperty<E>) {
        if (d.allowEmpty) {
            builder.defineListAllowEmpty(name, d.default, d.newElement, d.elementValidator)
        } else {
            builder.defineList(name, d.default, d.newElement, d.elementValidator)
        }.bind(d)
    }

    private fun <T : Comparable<T>> bind(name: String, d: RangedConfigProperty<T>) {
        @Suppress("UNCHECKED_CAST")
        builder.defineInRange(name, d.default, d.range.start, d.range.endInclusive, d.default::class.java as Class<T>)
            .bind(d)
    }

    private fun <T : Any> bind(name: String, d: OneOfConfigProperty<T>) {
        builder.defineInList(name, d.default, d.allowedValues).bind(d)
    }
}