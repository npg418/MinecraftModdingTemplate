package com.npg418.examplemod

import com.mojang.logging.LogUtils
import org.slf4j.Logger

class ExampleMod {
    companion object {
        const val MODID = "examplemod"
        val LOGGER: Logger = LogUtils.getLogger()
    }
}