plugins {
    alias(conventions.plugins.common.convention)
    alias(libs.plugins.fabric.loom)
}

expandProperties {
    put("fabric_loader_version_range", fabricLibs.versions.fabric.loader.range)
    put("minecraft_version_range", fabricLibs.versions.minecraft.range)
    put("java_version_range", java.toolchain.languageVersion.map { ">=$it" })
    put("fabric_api_version_range", fabricLibs.versions.fabric.api.range)
    put("fabric_kotlin_version_range", fabricLibs.versions.fabric.kotlin.range)
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

loom {
    log4jConfigs.from("log4j-dev.xml")

    runs {
        configureEach {
            jvmArguments.addAll(hotswapJvmArgs)

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
    maven("https://maven.terraformersmc.com") {
        name = "Terraformers"
    }
}

dependencies {
    implementation(projects.common)

    minecraft(libs.minecraft)
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${libs.versions.parchment.minecraft.get()}:${libs.versions.parchment.mapping.get()}@zip")
    })
    modImplementation(fabricLibs.fabric.loader)
    modImplementation(fabricLibs.fabric.api)
    modImplementation(fabricLibs.fabric.kotlin)

    modImplementation(fabricLibs.modmenu)
}
