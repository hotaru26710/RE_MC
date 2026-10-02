package org.hotaru.re_mc.deathreturn.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.hotaru.re_mc.deathreturn.client.UnseenHandClientEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record UnseenHandVisualPacket(List<Integer> entityIds, int durationTicks) {
    public static void encode(UnseenHandVisualPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityIds.size());
        for (int entityId : packet.entityIds) {
            buffer.writeVarInt(entityId);
        }
        buffer.writeVarInt(packet.durationTicks);
    }

    public static UnseenHandVisualPacket decode(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        List<Integer> entityIds = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entityIds.add(buffer.readVarInt());
        }
        return new UnseenHandVisualPacket(entityIds, buffer.readVarInt());
    }

    public static void handle(UnseenHandVisualPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> UnseenHandClientEffects.show(packet.entityIds, packet.durationTicks)));
        context.setPacketHandled(true);
    }
}