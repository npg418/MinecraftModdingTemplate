package com.npg418.gradle.modding

import libs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    idea
}

group = prop("mod_group_id").get()
version = prop("mod_version").get()

base.archivesName = prop("mod_id")

java {
    toolchain {
        languageVersion = prop("java_version").map { JavaLanguageVersion.of(it) }
        @Suppress("UnstableApiUsage")
        vendor = JvmVendorSpec.JETBRAINS
    }
    withSourcesJar()
    withJavadocJar()
}

val hotswapJvmArgs = provider {
    val mixinJar = configurations.runtimeClasspath.get()
        .copyRecursive()
        .resolvedConfiguration
        .resolvedArtifacts
        .first { artifact ->
            artifact.moduleVersion.id.let {
                it.group == "net.fabricmc" && it.name == "sponge-mixin"
                        || it.group == "org.spongepowered" && it.name == "mixin"
            }
        }
        .file.absolutePath

    listOf(
        "-XX:+AllowEnhancedClassRedefinition",
        "-javaagent:$mixinJar"
    )
}
extensions.add<Provider<List<String>>>("hotswapJvmArgs", hotswapJvmArgs)

val generatedResourcesDir = layout.buildDirectory.dir("generated/resources")
sourceSets.main {
    resources.srcDir(generatedResourcesDir)
}
extensions.add<Provider<Directory>>("generatedResourcesDir", generatedResourcesDir)

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

val modulePrefix = project.name
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.moduleName.set("${modulePrefix}_$name")
}