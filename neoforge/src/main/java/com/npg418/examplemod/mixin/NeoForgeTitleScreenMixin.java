package com.npg418.examplemod.mixin;

import com.npg418.examplemod.ExampleMod;
import com.npg418.examplemod.config.NeoForgeCommonConfig;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class NeoForgeTitleScreenMixin {
    @Inject(method = "init", at = @At("HEAD"))
    private void init(CallbackInfo ci) {
        if (NeoForgeCommonConfig.INSTANCE.getGreeting().getNeoForgeGreetOnTitleScreen().get()) {
            ExampleMod.INSTANCE.getLOGGER().debug("Hello from NeoForgeTitleScreenMixin!");
        }
    }
}
