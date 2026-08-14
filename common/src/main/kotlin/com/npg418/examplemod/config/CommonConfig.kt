package com.npg418.examplemod.config

import com.npg418.examplemod.ExampleMod
import com.npg418.examplemod.config.api.ConfigSection
import com.npg418.examplemod.config.api.ConfigSpec
import com.npg418.examplemod.config.api.ConfigType

object CommonConfig : ConfigSpec(ExampleMod.MODID, ConfigType.COMMON) {
    class GreetingSection : ConfigSection("greeting") {
        override var comment: String? = "Greeting log settings"
        val greetOnTitleScreen by define("greetOnTitleScreen", true) {
            comment = "Whether log when title screen"
        }
    }

    val greeting = section(::GreetingSection)
}