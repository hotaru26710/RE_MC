package org.hotaru.re_mc.deathreturn.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.hotaru.re_mc.deathreturn.client.ReturnTransitionOverlay;

import java.util.function.Supplier;

public record ReturnTransitionPacket(ReturnTransitionPhase phase, int durationTicks, float intensity) {
    public static void encode(ReturnTransitionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.phase);
        buffer.writeVarInt(Math.max(0, packet.durationTicks));
        buffer.writeFloat(Math.max(0.0F, packet.intensity));
    }

    public static ReturnTransitionPacket decode(FriendlyByteBuf buffer) {
        return new ReturnTransitionPacket(buffer.readEnum(ReturnTransitionPhase.class), buffer.readVarInt(), buffer.readFloat());
    }

    public static void handle(ReturnTransitionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ReturnTransitionOverlay.start(packet.phase, packet.durationTicks, packet.intensity)));
        context.setPacketHandled(true);
    }
}