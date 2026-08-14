package com.npg418.examplemod

import com.npg418.examplemod.config.CommonConfig
import com.npg418.examplemod.config.ConfigRegistry
import com.npg418.examplemod.config.StartupConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ExampleMod {
    const val MODID = "examplemod"
    val LOGGER: Logger = LoggerFactory.getLogger(MODID)

    init {
        ConfigRegistry.register(CommonConfig)
        ConfigRegistry.register(StartupConfig)
    }
}