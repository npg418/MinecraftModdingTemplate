plugins {
    alias(conventions.plugins.common.convention)
    alias(libs.plugins.fabric.loom)
}

loom {
    runs {
        configureEach {
            jvmArguments.add("-XX:+AllowEnhancedClassRedefinition")
            displayName = "Fabric: Run $name"
            appendProjectPathToDisplayName = false
            generateRunConfig = true
        }
    }

    mods {
        register(providers.gradleProperty("mod_id").get()) {
            sourceSet(sourceSets.main.get())
            sourceSet(project(":common").sourceSets.main.get())
        }
    }
}

repositories {
    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }
}

dependencies {
    minecraft(libs.minecraft)
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${libs.versions.parchment.minecraft.get()}:${libs.versions.parchment.mapping.get()}@zip")
    })
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.kotlin)

    implementation(projects.common)
}

extraModProperties.putAll(
    mapOf(
        "fabric_loader_version_range" to libs.versions.fabric.loader.range.get(),
        "minecraft_version_range" to libs.versions.minecraft.range.fabric.get(),
        "java_version_range" to ">=${java.toolchain.languageVersion.get()}",
        "fabric_api_version_range" to libs.versions.fabric.api.range.get(),
        "fabric_kotlin_version_range" to libs.versions.fabric.kotlin.range.get(),
    )
)
