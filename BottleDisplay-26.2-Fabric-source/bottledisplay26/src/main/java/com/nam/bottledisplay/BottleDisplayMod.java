package com.nam.bottledisplay;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.math.Transformation;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class BottleDisplayMod implements ModInitializer {
    public static final String MOD_ID = "bottledisplay";
    public static final String TAG = "bottledisplay";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(PlaceBottlePayload.TYPE, PlaceBottlePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PlaceBottlePayload.TYPE, (payload, context) -> {
            context.player().level().getServer().execute(() -> place(context.player(), payload));
        });
    }

    public static boolean isBottle(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.GLASS_BOTTLE
                || item == Items.POTION
                || item == Items.SPLASH_POTION
                || item == Items.LINGERING_POTION
                || item == Items.HONEY_BOTTLE;
    }

    private static void place(ServerPlayer player, PlaceBottlePayload p) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!isBottle(player.getMainHandItem())) return;
        if (p.face() != Direction.UP) return;

        BlockPos target = p.pos();
        if (!level.isLoaded(target)) return;
        if (!level.getBlockState(target).isCollisionShapeFullBlock(level, target)) return;
        if (player.distanceToSqr(target.toCenterPos()) > 36.0) return;

        AABB scan = new AABB(target);
        long count = level.getEntitiesOfClass(ItemDisplay.class, scan, e -> e.getcommandTags().contains(TAG)).size();
        if (count >= 4) return;

        double[][] offsets = {
                {0.00, 0.00},
                {-0.16, -0.16},
                {0.16, -0.16},
                {0.16, 0.16}
        };
        if (count == 2) { offsets[2][0] = 0.00; offsets[2][1] = 0.17; }

        double ox = offsets[(int) count][0];
        double oz = offsets[(int) count][1];

        ItemDisplay display = EntityType.ITEM_DISPLAY.create(level, EntitySpawnReason.TRIGGERED);
        if (display == null) return;
        display.setPos(target.getX() + 0.5 + ox, target.getY() + 1.0, target.getZ() + 0.5 + oz);
        display.setItemStack(player.getMainHandItem().copyWithCount(1));
        display.addTag(TAG);
        display.addTag(TAG + "_block_" + target.asLong());
        display.setTransformation(transform(p.yawQuarter(), p.lying()));
        display.setTransformationInterpolationDuration(0);
        level.addFreshEntity(display);

        if (!player.getAbilities().instabuild) player.getMainHandItem().shrink(1);
    }

    private static AffineTransformation transform(int yawQuarter, boolean lying) {
        float yaw = Mth.wrapDegrees((yawQuarter & 3) * 90.0f);
        Quaternionf rot = new Quaternionf().rotateY((float) Math.toRadians(yaw));
        if (lying) rot.rotateX((float) Math.toRadians(90));
        return new AffineTransformation(new Vector3f(), rot, new Vector3f(0.5f, 0.5f, 0.5f), new Quaternionf());
    }

    public static void removeAndGive(Entity entity, ServerPlayer player) {
        if (!(entity instanceof ItemDisplay display) || !display..contains(TAG)) return;
        ItemStack stack = display.getItemStack().copy();
        display.discard();
        if (!stack.isEmpty() && !player.getInventory().add(stack)) player.drop(stack, false);
    }
}
