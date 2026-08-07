package com.npg418.examplemod

import com.npg418.examplemod.init.ITEM_MAP
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.registries.DeferredRegister

@Mod(ExampleMod.MODID)
class NeoforgeMain(eventBus: IEventBus, container: ModContainer) {
    companion object {
        val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(ExampleMod.MODID)
    }

    init {
        ExampleMod.LOGGER.debug("Hello from Neoforge Mod!")

        ITEM_MAP.forEach(ITEMS::register)
        ITEMS.register(eventBus)
    }
}