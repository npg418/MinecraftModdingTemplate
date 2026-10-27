package com.npg418.gradle.modding

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.toml.TomlFormat
import com.electronwill.nightconfig.toml.TomlWriter
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

abstract class NeoForgeModsToml @Inject constructor(private val objects: ObjectFactory) {
    @get:Input
    abstract val modLoader: Property<String>

    @get:Input
    abstract val loaderVersion: Property<String>

    @get:Input
    abstract val license: Property<String>

    @get:Input
    @get:Optional
    abstract val showAsResourcePack: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val showAsDataPack: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val services: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val properties: MapProperty<String, String>

    @get:Input
    @get:Optional
    abstract val issueTrackerURL: Property<String>

    @get:Nested
    abstract val mods: ListProperty<NeoForgeModSpec>

    fun mod(modId: String, action: Action<in NeoForgeModSpec>? = null) = create(mods) {
        this.modId.set(modId)
        action?.execute(this)
    }

    @get:Nested
    abstract val features: NamedDomainObjectContainer<NeoForgeModFeaturesSpec>

    fun features(modId: String, action: Action<in NeoForgeModFeaturesSpec>) = features.register(modId, action)

    @get:Nested
    abstract val modproperties: NamedDomainObjectContainer<NeoForgeModPropertiesSpec>

    fun modproperties(modId: String, vararg properties: Pair<String, String>) = modproperties(modId, mapOf(*properties))

    fun modproperties(modId: String, properties: Map<String, String>) = modproperties.register(modId) {
        putAll(properties)
    }

    @get:Input
    abstract val accessTransformers: ListProperty<NeoForgeAccessTransformerSpec>

    fun accessTransformer(file: String) = create(accessTransformers) { this.file.set(file) }

    @get:Input
    abstract val mixins: ListProperty<NeoForgeMixinSpec>

    fun mixin(config: String) = create(mixins) { this.config.set(config) }

    private inline fun <reified T : Any> create(listProperty: ListProperty<T>, action: Action<T>) {
        val item = objects.newInstance<T>()
        action.execute(item)
        listProperty.add(item)
    }

    @get:Nested
    abstract val dependencies: NamedDomainObjectContainer<NeoForgeModDependenciesSpec>

    fun dependencies(modId: String, action: Action<NeoForgeModDependenciesSpec>) = dependencies.register(modId, action)

    internal fun toConfig() = config {
        put("modLoader", modLoader)
        put("loaderVersion", loaderVersion)
        put("license", license)
        put("showAsResourcePack", showAsResourcePack)
        put("showAsDataPack", showAsDataPack)
        put("services", services)
        put("properties", properties.map { props ->
            config {
                props.forEach { (k, v) ->
                    set(k, v)
                }
            }
        })
        put("issueTrackerURL", issueTrackerURL)
        put("mods", mods.map { modSpecs -> modSpecs.map { it.toConfig() } })
        putNamed("features", features) { it.toConfig() }
        putNamed("modproperties", modproperties) { spec ->
            config {
                spec.forEach { (k, v) -> set(listOf(k), v) }
            }
        }
        put("accessTransformers", accessTransformers.map { specs -> specs.map { it.toConfig() } })
        put("mixins", mixins.map { mixinSpecs -> mixinSpecs.map { it.toConfig() } })
        putNamed("dependencies", dependencies) { specs -> specs.map { it.toConfig() } }
    }
}

abstract class NeoForgeModSpec @Inject constructor() {
    @get:Input
    abstract val modId: Property<String>

    @get:Input
    @get:Optional
    abstract val namespace: Property<String>

    @get:Input
    @get:Optional
    abstract val version: Property<String>

    @get:Input
    @get:Optional
    abstract val displayName: Property<String>

    @get:Input
    @get:Optional
    abstract val description: Property<String>

    @get:Input
    @get:Optional
    abstract val logoFile: Property<String>

    @get:Input
    @get:Optional
    abstract val logoBlur: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val updateJSONURL: Property<String>

    @get:Input
    @get:Optional
    abstract val credits: Property<String>

    @get:Input
    @get:Optional
    abstract val authors: Property<String>

    @get:Input
    @get:Optional
    abstract val displayURL: Property<String>

    @get:Input
    @get:Optional
    abstract val enumExtensions: Property<String>

    internal fun toConfig() = config {
        put("modId", modId)
        put("namespace", namespace)
        put("version", version)
        put("displayName", displayName)
        put("description", description)
        put("logoFile", logoFile)
        put("logoBlur", logoBlur)
        @Suppress("SpellCheckingInspection")
        put("updateJSONURL", updateJSONURL)
        put("credits", credits)
        put("authors", authors)
        put("displayURL", displayURL)
        put("enumExtensions", enumExtensions)
    }
}

abstract class NeoForgeModFeaturesSpec @Inject constructor(private val modId: String) : Named {
    @Internal
    override fun getName(): String = modId

    @get:Input
    @get:Optional
    abstract val javaVersion: Property<String>

    @get:Input
    @get:Optional
    abstract val openGLVersion: Property<String>

    internal fun toConfig() = config {
        put("javaVersion", javaVersion)
        put("openGLVersion", openGLVersion)
    }
}

abstract class NeoForgeModPropertiesSpec @Inject constructor(private val modId: String) : HashMap<String, String>(),
    Named {
    @Internal
    override fun getName(): String = modId
}

abstract class NeoForgeAccessTransformerSpec @Inject constructor() {
    @get:Input
    @get:Optional
    abstract val file: Property<String>

    internal fun toConfig() = config {
        put("file", file)
    }
}

abstract class NeoForgeMixinSpec @Inject constructor() {
    @get:Input
    @get:Optional
    abstract val config: Property<String>

    internal fun toConfig() = config {
        put("config", config)
    }
}

enum class DependencyType { REQUIRED, OPTIONAL, INCOMPATIBLE, DISCOURAGED }
enum class Ordering { NONE, BEFORE, AFTER }
enum class Side { BOTH, CLIENT, SERVER }
abstract class NeoForgeModDependencySpec @Inject constructor() {

    @get:Input
    abstract val modId: Property<String>

    @get:Input
    @get:Optional
    abstract val type: Property<DependencyType>

    @get:Input
    @get:Optional
    abstract val reason: Property<String>

    @get:Input
    @get:Optional
    abstract val versionRange: Property<String>

    @get:Input
    @get:Optional
    abstract val ordering: Property<Ordering>

    @get:Input
    @get:Optional
    abstract val side: Property<Side>

    internal fun toConfig() = config {
        put("modId", modId)
        put("type", type.map { it.name.lowercase() })
        put("reason", reason)
        put("versionRange", versionRange)
        put("ordering", ordering.map { it.name })
        put("side", side.map { it.name })
    }
}

abstract class NeoForgeModDependenciesSpec @Inject constructor(
    private val modId: String,
    private val objects: ObjectFactory,
) : ArrayList<NeoForgeModDependencySpec>(), Named {
    @Internal
    override fun getName(): String = modId

    fun depends(modId: String, action: Action<in NeoForgeModDependencySpec>? = null) {
        add(objects.newInstance<NeoForgeModDependencySpec>().also {
            it.modId.set(modId)
            action?.execute(it)
        })
    }
}

private fun config(block: Config.() -> Unit): Config = TomlFormat.newConfig().apply(block)
private fun <T : Any> Config.put(key: String, provider: Provider<T>) {
    provider.orNull?.let {
        if (it is ListProperty<*> && it.get().isEmpty()) return@let
        if (it is MapProperty<*, *> && it.get().isEmpty()) return@let
        set<T>(key, it)
    }
}

private fun <T : Named> Config.putNamed(key: String, items: Iterable<T>, convert: (T) -> Any) {
    if (!items.iterator().hasNext()) return
    set<Any>(key, config {
        items.forEach { set(listOf(it.name), convert(it)) }
    })
}

abstract class NeoForgeModsTomlTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Nested
    val toml: NeoForgeModsToml = objects.newInstance(NeoForgeModsToml::class.java)

    fun toml(action: Action<in NeoForgeModsToml>) = action.execute(toml)

    @TaskAction
    fun generate() {
        outputFile.get().asFile.bufferedWriter().use {
            TomlWriter().write(toml.toConfig(), it)
        }
    }
}