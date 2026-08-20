package com.nam.bottledisplay.mixin;

import com.nam.bottledisplay.BottleDisplayMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class ItemDisplayDamageMixin {
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self.getScoreboardTags().contains(BottleDisplayMod.TAG)) {
            ServerPlayer player = source.getEntity() instanceof ServerPlayer p ? p : null;
            BottleDisplayMod.removeAndGive(self, player);
            cir.setReturnValue(true);
        }
    }
}
