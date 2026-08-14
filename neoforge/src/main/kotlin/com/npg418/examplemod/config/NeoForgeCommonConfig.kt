package com.npg418.examplemod.config

import com.npg418.examplemod.ExampleMod

object NeoForgeCommonConfig : ConfigSpec(ExampleMod.MODID, ConfigType.COMMON) {
    object NeoForgeGreetingSection : ConfigSection(this, "greeting") {
        val greetOnTitleScreen: ConfigEntry<Boolean> = define(
            "greetOnTitleScreen",
            false,
            "Whether log when title screen from common module"
        )
        val neoForgeGreetOnTitleScreen: ConfigEntry<Boolean> = define(
            "neoForgeGreetOnTitleScreen",
            true,
            "Whether log when title screen from neoforge module"
        )
    }

    val greeting = NeoForgeGreetingSection
}