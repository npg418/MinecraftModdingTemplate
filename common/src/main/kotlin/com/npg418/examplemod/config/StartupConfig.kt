package com.npg418.examplemod.config

import com.npg418.examplemod.ExampleMod

object StartupConfig : ConfigSpec(ExampleMod.MODID, ConfigType.STARTUP) {
    object ItemSection : ConfigSection(this, "item") {
        val exampleItemDurability: RangedConfigEntry<Int> = defineInRange(
            "exampleItemDurability",
            64,
            1..Int.MAX_VALUE,
            "Durability of Example Item"
        )
    }

    val item = ItemSection
}