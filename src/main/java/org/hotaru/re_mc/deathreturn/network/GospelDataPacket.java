package org.hotaru.re_mc.deathreturn.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.hotaru.re_mc.deathreturn.client.GospelScreen;
import org.hotaru.re_mc.deathreturn.memory.DeathRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record GospelDataPacket(float spirit, float witchScent, int totalReturns, long lastCheckpointTime, String lastCheckpointDimension, List<DeathRecord> deaths) {
    public static void encode(GospelDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.spirit);
        buffer.writeFloat(packet.witchScent);
        buffer.writeVarInt(packet.totalReturns);
        buffer.writeLong(packet.lastCheckpointTime);
        buffer.writeUtf(packet.lastCheckpointDimension, 256);
        buffer.writeVarInt(packet.deaths.size());
        for (DeathRecord death : packet.deaths) {
            buffer.writeLong(death.getGameTime());
            buffer.writeVarInt(death.getDeathCount());
            buffer.writeUtf(death.getCause(), 512);
            buffer.writeUtf(death.getDimension(), 256);
            buffer.writeDouble(death.getX());
            buffer.writeDouble(death.getY());
            buffer.writeDouble(death.getZ());
        }
    }

    public static GospelDataPacket decode(FriendlyByteBuf buffer) {
        float spirit = buffer.readFloat();
        float witchScent = buffer.readFloat();
        int totalReturns = buffer.readVarInt();
        long checkpointTime = buffer.readLong();
        String dimension = buffer.readUtf(256);
        int count = Math.min(buffer.readVarInt(), 10);
        List<DeathRecord> deaths = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            deaths.add(new DeathRecord(
                    buffer.readLong(),
                    buffer.readVarInt(),
                    buffer.readUtf(512),
                    buffer.readUtf(256),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
            ));
        }
        return new GospelDataPacket(spirit, witchScent, totalReturns, checkpointTime, dimension, deaths);
    }

    public static void handle(GospelDataPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> GospelScreen.open(packet)));
        context.setPacketHandled(true);
    }
}
