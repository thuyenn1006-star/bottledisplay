package com.nam.bottledisplay;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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

    private static void place(ServerPlayer player, PlaceBottlePayload p) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!isBottle(player.getMainHandItem())) return;
        if (p.face() != Direction.UP) return;

        BlockPos target = p.pos();
        if (!level.isLoaded(target)) return;
        if (!level.getBlockState(target).isCollisionShapeFullBlock(level, target)) return;
        if (player.distanceToSqr(Vec3.atCenterOf(target)) > 36.0) return;

        AABB scan = new AABB(target);
        long count = level.getEntitiesOfClass(ItemDisplay.class, scan, e -> e.getScoreboardTags().contains(TAG)).size();
        if (count >= 4) return;

        double[][] offsets = {
            {0.00, 0.00},
            {-0.20, 0.20},
            {0.20, 0.20},
            {0.00, -0.20}
        };

        double ox = offsets[(int) count][0];
        double oz = offsets[(int) count][1];

        EntityType<ItemDisplay> type = (EntityType<ItemDisplay>) BuiltInRegistries.ENTITY_TYPE.getValue(ResourceLocation.withDefaultNamespace("item_display"));
        if (type == null) return;

        ItemDisplay display = type.create(level, EntitySpawnReason.TRIGGERED);
        if (display == null) return;

        display.setPos(target.getX() + 0.5 + ox, target.getY() + 1.0, target.getZ() + 0.5 + oz);
        display.setItemStack(player.getMainHandItem().split(1));
        display.addTag(TAG);
        display.setTransformation(transform(p.yawQuarter(), p.lying()));
        display.setTransformationInterpolationDuration(0);

        level.addFreshEntity(display);
    }

    private static Transformation transform(int yawQuarter, boolean lying) {
        float yaw = yawQuarter * 90.0f;
        Quaternionf rot = new Quaternionf().rotationY((float) Math.toRadians(-yaw));

        if (lying) {
            rot.rotateX((float) Math.toRadians(-90));
        }

        return new Transformation(new Vector3f(), rot, new Vector3f(0.5f, 0.5f, 0.5f), new Quaternionf());
    }

    public static void removeBottle(Entity entity) {
        removeAndGive(entity, null);
    }

    public static void removeAndGive(Entity entity, ServerPlayer player) {
        if (!(entity instanceof ItemDisplay display) || !display.getScoreboardTags().contains(TAG)) return;

        if (entity.level() instanceof ServerLevel level) {
            ItemStack stack = display.getItemStack();
            if (!stack.isEmpty()) {
                if (player != null && !player.getInventory().add(stack)) {
                    Block.popResource(level, display.blockPosition(), stack);
                } else if (player == null) {
                    Block.popResource(level, display.blockPosition(), stack);
                }
            }
            display.discard();
        }
    }
}
