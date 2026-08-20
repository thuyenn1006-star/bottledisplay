package com.nam.bottledisplay.client;

import com.nam.bottledisplay.BottleDisplayMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseScrollMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void bottledisplay$scroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (BottleDisplayClient.handleScroll(client, vertical)) ci.cancel();
    }
}
