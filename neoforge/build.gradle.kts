import org.slf4j.event.Level

plugins {
    alias(conventions.plugins.common.convention)
    alias(libs.plugins.moddev.gradle)
}

neoForge {
    version = libs.versions.neoforge.exact.get()

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

        create("data") {
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

            val mixinJar = configurations.runtimeClasspath.get()
                .copyRecursive()
                .resolvedConfiguration
                .resolvedArtifacts
                .first { it.moduleVersion.id.group == "net.fabricmc" && it.moduleVersion.id.name == "sponge-mixin" }
                .file.absolutePath

            jvmArguments = listOf("-XX:+AllowEnhancedClassRedefinition", "-javaagent:$mixinJar")

            ideName = "NeoForge: Run ${type.get()}"
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
}

dependencies {
    implementation(libs.kotlinforforge)

    implementation(projects.common)
}

extraModProperties.putAll(
    mapOf(
        "loader_version_range" to libs.versions.kotlinforforge.range.get(),
        "neoforge_version_range" to libs.versions.neoforge.range.get(),
        "minecraft_version_range" to libs.versions.minecraft.range.neoforge.get()
    )
)