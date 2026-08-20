package com.nam.bottledisplay;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlaceBottlePayload(BlockPos pos, Direction face, int yawQuarter, boolean lying)
        implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(BottleDisplayMod.MOD_ID, "place_bottle");
    public static final Type<PlaceBottlePayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceBottlePayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PlaceBottlePayload::pos,
                    Direction.STREAM_CODEC, PlaceBottlePayload::face,
                    ByteBufCodecs.INT, PlaceBottlePayload::yawQuarter,
                    ByteBufCodecs.BOOL, PlaceBottlePayload::lying,
                    PlaceBottlePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
