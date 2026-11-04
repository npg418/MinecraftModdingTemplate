package com.npg418.examplemod

import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLLoader

class NeoForgePlatformHelper : PlatformHelper {
    override val isDevEnvironment: Boolean get() = !FMLLoader.isProduction()

    override fun isModLoaded(id: String): Boolean = ModList.get().isLoaded(id)
}