package org.hotaru.re_mc.deathreturn.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.hotaru.re_mc.deathreturn.client.ReturnTransitionOverlay;

import java.util.function.Supplier;

public record ReturnTransitionPacket(int ticks, boolean reveal) {
    public static void encode(ReturnTransitionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.ticks);
        buffer.writeBoolean(packet.reveal);
    }

    public static ReturnTransitionPacket decode(FriendlyByteBuf buffer) {
        return new ReturnTransitionPacket(buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(ReturnTransitionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ReturnTransitionOverlay.start(packet.ticks, packet.reveal)));
        context.setPacketHandled(true);
    }
}
