package org.hotaru.re_mc.deathreturn.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.hotaru.re_mc.deathreturn.spirit.UnseenHandService;

import java.util.function.Supplier;

public record UnseenHandActivatePacket() {
    public static void encode(UnseenHandActivatePacket packet, FriendlyByteBuf buffer) {
    }

    public static UnseenHandActivatePacket decode(FriendlyByteBuf buffer) {
        return new UnseenHandActivatePacket();
    }

    public static void handle(UnseenHandActivatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                UnseenHandService.activate(sender);
            }
        });
        context.setPacketHandled(true);
    }
}