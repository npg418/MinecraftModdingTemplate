package com.npg418.examplemod.config

import com.npg418.examplemod.ExampleMod
import com.npg418.examplemod.config.api.ConfigSection
import com.npg418.examplemod.config.api.ConfigSpec
import com.npg418.examplemod.config.api.ConfigType

object StartupConfig : ConfigSpec(ExampleMod.MODID, ConfigType.STARTUP) {
    class ItemSection : ConfigSection() {
        val exampleItemDurability by defineInRange("exampleItemDurability", 64, 1..Int.MAX_VALUE) {
            comment = "Durability of Example Item"
            gameRestart = true
        }
    }

    val item = section("item", ::ItemSection)
}