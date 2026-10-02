package org.hotaru.re_mc.deathreturn.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.hotaru.re_mc.deathreturn.client.WitchCurseOverlay;

import java.util.function.Supplier;

public record WitchCursePacket(float darkness, boolean active) {
    public static void encode(WitchCursePacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.darkness);
        buffer.writeBoolean(packet.active);
    }

    public static WitchCursePacket decode(FriendlyByteBuf buffer) {
        return new WitchCursePacket(buffer.readFloat(), buffer.readBoolean());
    }

    public static void handle(WitchCursePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> WitchCurseOverlay.set(packet.darkness, packet.active)));
        context.setPacketHandled(true);
    }
}
