package com.nam.bottledisplay.mixin;

import com.nam.bottledisplay.BottleDisplayMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemDisplay.class)
public abstract class ItemDisplayDamageMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void bottledisplay$hurt(ServerPlayer player, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self.getTags().contains(BottleDisplayMod.TAG)) {
            BottleDisplayMod.removeAndGive(self, player);
            cir.setReturnValue(true);
        }
    }
}
