package com.npg418.examplemod.config

import com.npg418.examplemod.ExampleMod

object CommonConfig : ConfigSpec(ExampleMod.MODID, ConfigType.COMMON) {
    object GreetingSection : ConfigSection(this, "greeting") {
        val greetOnTitleScreen: ConfigEntry<Boolean> = define(
            "greetOnTitleScreen",
            true,
            "Whether log when title screen"
        )
    }

    val greeting = GreetingSection
}