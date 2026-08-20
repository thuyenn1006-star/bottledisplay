package com.nam.bottledisplay.mixin;

import com.nam.bottledisplay.BottleDisplayMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class ItemDisplayDamageMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self.getCommandTags().contains(BottleDisplayMod.TAG)) {
            ServerPlayerEntity player = source.getAttacker() instanceof ServerPlayerEntity p ? p : null;
            BottleDisplayMod.removeAndGive(self, player);
            cir.setReturnValue(true);
        }
    }
}
