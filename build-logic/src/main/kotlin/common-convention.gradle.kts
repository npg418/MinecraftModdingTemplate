import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
    alias(libs.plugins.kotlin.jvm)
    idea
}

group = providers.gradleProperty("mod_group_id").get()
version = providers.gradleProperty("mod_version").get()

base.archivesName = providers.gradleProperty("mod_id")

java.toolchain {
    languageVersion = JavaLanguageVersion.of(21)
    @Suppress("UnstableApiUsage")
    vendor = JvmVendorSpec.JETBRAINS
}

val hotswapJvmArgs = provider {
    val mixinJar = configurations.runtimeClasspath.get()
        .copyRecursive()
        .resolvedConfiguration
        .resolvedArtifacts
        .first { it.moduleVersion.id.group == "net.fabricmc" && it.moduleVersion.id.name == "sponge-mixin" }
        .file.absolutePath

    listOf(
        "-XX:+AllowEnhancedClassRedefinition",
        "-javaagent:$mixinJar"
    )
}
extensions.add<Provider<List<String>>>("hotswapJvmArgs", hotswapJvmArgs)

val extraModProperties: MapProperty<String, String> = objects.mapProperty(String::class, String::class)
extensions.add<MapProperty<String, String>>("extraModProperties", extraModProperties)

val baseProperties = provider {
    listOf(
        "mod_id",
        "mod_name",
        "mod_description",
        "mod_authors",
        "mod_license",
        "mod_version",
        "mod_group_id"
    ).associateWith { providers.gradleProperty(it).get() }
}

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    description = "Generate mod metadata file from template"

    val replaceProperties = baseProperties.get() + extraModProperties.get()

    inputs.properties(replaceProperties)
    expand(replaceProperties)

    from("src/main/templates")
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}

val cleanModMetadataOnFailure = tasks.register<Delete>("cleanModMetadataOnFailure") {
    description = "Delete generated mod metadata on failure"

    onlyIf { (generateModMetadata.get().state.failure as Throwable?) != null }
    delete(generateModMetadata.map { it.outputs.files })
}

generateModMetadata.configure { finalizedBy(cleanModMetadataOnFailure) }

sourceSets.main {
    resources.srcDir(generateModMetadata)
    resources.srcDir("src/generated/resources")
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

rootProject.pluginManager.apply(libs.plugins.idea.ext.get().pluginId)
rootProject.extensions.configure<IdeaModel> {
    project {
        settings {
            taskTriggers {
                beforeSync(generateModMetadata)
            }
        }
    }
}