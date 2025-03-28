@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        exclusiveContent {
            forRepository {
                maven("https://maven.fabricmc.net/") {
                    name = "Fabric"
                }
            }
            filter {
                includeGroup("net.fabricmc")
                includeGroup("fabric-loom")
            }
        }
        exclusiveContent {
            forRepository {
                maven("https://repo.spongepowered.org/repository/maven-public/") {
                    name = "Sponge"
                }
            }
            filter {
                includeGroupAndSubgroups("org.spongepowered")
            }
        }
        exclusiveContent {
            forRepository {
                maven("https://maven.minecraftforge.net/") {
                    name = "Forge"
                }
            }
            filter {
                includeGroupAndSubgroups("net.minecraftforge")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        exclusiveContent {
            forRepository {
                maven("https://repo.spongepowered.org/repository/maven-public/") {
                    name = "Sponge"
                }
            }
            filter {
                includeGroupAndSubgroups("org.spongepowered")
            }
        }
        exclusiveContent {
            forRepositories(
                maven("https://maven.parchmentmc.org/") {
                    name = "ParchmentMC"
                },
                maven("https://maven.neoforged.net/releases/") {
                    name = "NeoForge"
                }
            )
            filter {
                includeGroup("org.parchmentmc.data")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

// Replace with individual project name
rootProject.name = "MinecraftModdingTemplate"
include("common")
