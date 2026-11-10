package com.npg418.examplemod.config

import com.npg418.examplemod.config.api.*
import com.npg418.examplemod.items.GreetingLang

object CommonConfig : ConfigSpec(ConfigType.COMMON) {
    @Comment("Greeting log settings")
    @Name("greeting")
    object Greeting {
        @Comment("Whether log when title screen", "false is not acceptable")
        var greetOnTitleScreen by entry(true) withValidator { it }
    }

    @Name("item")
    object Item {
        @Comment("Greeting language when right-clicking Example Item")
        @Name("exampleItemGreetingLang")
        var greetingLang by entry(GreetingLang.EN)
    }
}