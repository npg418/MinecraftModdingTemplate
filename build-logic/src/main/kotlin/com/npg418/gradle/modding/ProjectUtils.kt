package com.npg418.gradle.modding

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

fun Project.prop(propertyName: String) = providers.gradleProperty(propertyName)

val Provider<MinimalExternalModuleDependency>.version
    get() = map { it.versionConstraint.preferredVersion }
val Provider<MinimalExternalModuleDependency>.versionRange
    get() = map { it.versionConstraint.strictVersion }