package com.npg418.examplemod

import net.fabricmc.api.ModInitializer

class FabricMain : ModInitializer {
    override fun onInitialize() {
        ExampleMod.LOGGER.debug("Hello from Fabric Mod!")
    }
}