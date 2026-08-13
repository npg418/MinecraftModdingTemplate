package com.npg418.examplemod.config

import com.npg418.examplemod.ExampleMod

object NeoForgeCommonConfig : ConfigSpec(ExampleMod.MODID, ConfigType.COMMON) {
    @Suppress("unused")
    val greetOnTitleScreen = define("greetOnTitleScreenNeoForge", true, "Whether log when title screen from common module")

    val greetOnTitleScreenNeoforge =
        define("greetOnTitleScreenNeoForge", true, "Whether log when title screen from neoforge module")
}