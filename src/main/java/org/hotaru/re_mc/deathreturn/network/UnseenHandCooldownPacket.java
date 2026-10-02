package org.hotaru.re_mc.deathreturn.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.hotaru.re_mc.deathreturn.client.UnseenHandClientEffects;

import java.util.function.Supplier;

public record UnseenHandCooldownPacket(int remainingTicks, boolean enabled) {
    public static void encode(UnseenHandCooldownPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(Math.max(0, packet.remainingTicks));
        buffer.writeBoolean(packet.enabled);
    }

    public static UnseenHandCooldownPacket decode(FriendlyByteBuf buffer) {
        return new UnseenHandCooldownPacket(buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(UnseenHandCooldownPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> UnseenHandClientEffects.setCooldown(packet.remainingTicks, packet.enabled)));
        context.setPacketHandled(true);
    }
}