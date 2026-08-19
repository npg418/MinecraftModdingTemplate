package com.npg418.examplemod.config

import com.npg418.examplemod.ExampleMod
import com.npg418.examplemod.config.api.ConfigSection
import com.npg418.examplemod.config.api.ConfigSpec
import com.npg418.examplemod.config.api.ConfigType
import com.npg418.examplemod.items.GreetingLang

object CommonConfig : ConfigSpec(ExampleMod.MODID, ConfigType.COMMON) {
    class GreetingSection : ConfigSection() {
        val greetOnTitleScreen by define("greetOnTitleScreen", true) {
            comment = "Whether log when title screen"
        }
    }

    val greeting = section("greeting", ::GreetingSection) {
        comment = "Greeting log settings"
    }

    class ItemSection : ConfigSection() {
        val exampleItemGreetingLang by defineEnum("exampleItemGreetingLang", GreetingLang.EN) {
            comment = "Greeting language when right-clicking Example Item"
        }
    }

    val item = section("item", ::ItemSection)
}