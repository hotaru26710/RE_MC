package org.hotaru.re_mc.deathreturn.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.hotaru.re_mc.deathreturn.client.SpiritHudOverlay;

import java.util.function.Supplier;

public record SpiritSyncPacket(float spirit, float witchScent, boolean enabled) {
    public static void encode(SpiritSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.spirit);
        buffer.writeFloat(packet.witchScent);
        buffer.writeBoolean(packet.enabled);
    }

    public static SpiritSyncPacket decode(FriendlyByteBuf buffer) {
        return new SpiritSyncPacket(buffer.readFloat(), buffer.readFloat(), buffer.readBoolean());
    }

    public static void handle(SpiritSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SpiritHudOverlay.setSpirit(packet.spirit, packet.witchScent, packet.enabled)));
        context.setPacketHandled(true);
    }
}
