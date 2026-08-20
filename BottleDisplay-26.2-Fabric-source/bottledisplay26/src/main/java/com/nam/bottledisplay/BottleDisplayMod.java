package com.nam.bottledisplay;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BottleDisplayMod implements ModInitializer {
    public static final String MOD_ID = "bottledisplay";
    public static final String TAG = "bottle_display";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(PlaceBottlePayload.TYPE, PlaceBottlePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PlaceBottlePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> place(context.player(), payload));
        });
    }

    private static boolean isBottle(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.GLASS_BOTTLE || item == Items.HONEY_BOTTLE;
    }

    private static void place(ServerPlayerEntity player, PlaceBottlePayload p) {
        if (!(player.getWorld() instanceof ServerWorld level)) return;
        if (!isBottle(player.getMainHandStack())) return;
        if (p.face() != Direction.UP) return;

        BlockPos target = p.pos();
        if (!level.isChunkLoaded(target)) return;
        if (!level.getBlockState(target).isFullCube(level, target)) return;
        if (player.squaredDistanceTo(Vec3d.ofCenter(target)) > 36.0) return;

        Box scan = new Box(target);
        long count = level.getEntitiesByClass(ItemDisplayEntity.class, scan, e -> e.getCommandTags().contains(TAG)).size();
        if (count >= 4) return;

        double[][] offsets = {
            {0.00, 0.00},
            {-0.20, 0.20},
            {0.20, 0.20},
            {0.00, -0.20}
        };

        double ox = offsets[(int) count][0];
        double oz = offsets[(int) count][1];

        ItemDisplayEntity display = EntityType.ITEM_DISPLAY.create(level, SpawnReason.TRIGGERED);
        if (display == null) return;

        display.refreshPositionAndAngles(target.getX() + 0.5 + ox, target.getY() + 1.0, target.getZ() + 0.5 + oz, 0, 0);
        display.setItemStack(player.getMainHandStack().split(1));
        display.addCommandTag(TAG);

        level.spawnEntity(display);
    }

    public static void removeBottle(Entity entity) {
        removeAndGive(entity, null);
    }

    public static void removeAndGive(Entity entity, ServerPlayerEntity player) {
        if (!(entity instanceof ItemDisplayEntity display) || !display.getCommandTags().contains(TAG)) return;

        if (entity.getWorld() instanceof ServerWorld level) {
            ItemStack stack = display.getItemStack();
            if (!stack.isEmpty()) {
                if (player != null && !player.getInventory().insertStack(stack)) {
                    Block.dropStack(level, display.getBlockPos(), stack);
                } else if (player == null) {
                    Block.dropStack(level, display.getBlockPos(), stack);
                }
            }
            display.discard();
        }
    }
}
