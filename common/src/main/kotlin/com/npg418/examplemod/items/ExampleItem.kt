package com.npg418.examplemod.items

import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class ExampleItem : Item(Properties().stacksTo(16)) {
    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack?> {
        if (level.isClientSide) {
            player.displayClientMessage(
                Component.literal("Hello ").append(player.name).append(Component.literal("!")),
                false
            )
        }
        return InteractionResultHolder.pass(player.getItemInHand(usedHand))
    }
}