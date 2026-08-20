package com.nam.bottledisplay.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nam.bottledisplay.BottleDisplayMod;
import com.nam.bottledisplay.PlaceBottlePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.lwjgl.glfw.GLFW;

public final class BottleDisplayClient implements ClientModInitializer {
    private static final KeyMapping PLACE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.bottledisplay.place",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(BottleDisplayMod.MOD_ID, "bottle_display"))
    ));

    private static final KeyMapping LIE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.bottledisplay.lie",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(BottleDisplayMod.MOD_ID, "bottle_display_pose"))
    ));

    private static int yawQuarter = 0;
    private static boolean lying = false;

    public static boolean handleScroll(Minecraft client, double vertical) {
        if (client.player == null || client.gui == null || client.gui.getCurrentScreen() != null) return false;
        if (!BottleDisplayMod.isBottle(client.player.getMainHandItem())) return false;
        if (vertical > 0) yawQuarter = (yawQuarter + 1) & 3;
        else if (vertical < 0) yawQuarter = (yawQuarter + 3) & 3;
        return vertical != 0;
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (PLACE.consumeClick()) place(client);
            while (LIE.consumeClick()) lying = !lying;
        });
    }

    private static void place(Minecraft client) {
        if (client.player == null || client.hitResult == null) return;
        ItemStack hand = client.player.getMainHandItem();
        if (!BottleDisplayMod.isBottle(hand)) return;
        if (!(client.hitResult instanceof BlockHitResult hit)) return;
        if (hit.getDirection() != Direction.UP) return;
        ClientPlayNetworking.send(new PlaceBottlePayload(hit.getBlockPos(), hit.getDirection(), yawQuarter, lying));
    }
}
