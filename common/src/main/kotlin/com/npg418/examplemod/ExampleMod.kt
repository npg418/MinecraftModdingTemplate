package com.npg418.examplemod

import com.npg418.examplemod.config.CommonModConfig
import com.npg418.examplemod.config.ConfigRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ExampleMod {
    companion object {
        const val MODID = "examplemod"
        val LOGGER: Logger = LoggerFactory.getLogger(MODID)
        fun init() {
            ConfigRegistry.register(CommonModConfig)
        }
    }
}