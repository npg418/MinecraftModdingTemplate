package com.npg418.gradle.modding

import libs

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
        .first { it.moduleVersion.id.group == "net.fabricmc" && it.moduleVersion.id.name == "sponge-mixin" }
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