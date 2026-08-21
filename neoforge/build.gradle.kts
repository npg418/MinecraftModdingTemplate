import org.slf4j.event.Level

plugins {
    alias(conventions.plugins.common.convention)
    alias(libs.plugins.moddev.gradle)
}

expandProperties {
    put("loader_version_range", neoforgeLibs.versions.kotlinforforge.range)
    put("neoforge_version_range", neoforgeLibs.versions.neoforge.range)
    put("minecraft_version_range", neoforgeLibs.versions.minecraft.range)
}

neoForge {
    version = neoforgeLibs.versions.neoforge.asProvider().get()

    parchment {
        minecraftVersion = libs.versions.parchment.minecraft
        mappingsVersion = libs.versions.parchment.mapping
    }

    runs {
        create("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", providers.gradleProperty("mod_id").get())
        }

        create("server") {
            server()
            systemProperty("neoforge.enabledGameTestNamespaces", providers.gradleProperty("mod_id").get())
        }

        create("datagen") {
            data()
            programArguments.addAll(
                "--mod",
                providers.gradleProperty("mod_id").get(),
                "--all",
                "--output",
                file("src/generated/resources/").absolutePath,
                "--existing",
                file("src/main/resources/").absolutePath
            )
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = Level.DEBUG

            jvmArguments.addAll(hotswapJvmArgs)

            ideName = "NeoForge: Run $name"
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
    maven("https://thedarkcolour.github.io/KotlinForForge") {
        name = "Kotlin for Forge"
    }
    maven("https://cursemaven.com") {
        name = "CurseMaven"
    }
}

dependencies {
    implementation(projects.common)

    implementation(neoforgeLibs.kotlinforforge)
    runtimeOnly(neoforgeLibs.configured)
}