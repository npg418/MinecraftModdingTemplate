package com.npg418.examplemod.config

import com.npg418.examplemod.ExampleMod

object CommonConfig : ConfigSpec(ExampleMod.MODID, ConfigType.COMMON) {
    object ItemSection : ConfigSection(this, "item") {
        val exampleItemDurability =
            defineInRange("exampleItemDurability", 64, 1..Int.MAX_VALUE, "Durability of Example Item")
    }

    val greetOnTitleScreen = define("greetOnTitleScreen", true, "Whether log when title screen")
}