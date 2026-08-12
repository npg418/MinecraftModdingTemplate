package com.npg418.examplemod.config

import com.npg418.examplemod.ExampleMod

object CommonModConfig : ConfigSpec(ExampleMod.MODID, ConfigType.COMMON) {
    val item = section("item") {
        defineInRange("example_item_durability", 64, 1..Int.MAX_VALUE, "Durability of Example Item")
    }

    val greetOnTitleScreen = define("greet_on_title_screen", true, "Whether log when title screen")
}