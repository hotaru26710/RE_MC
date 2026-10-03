package org.hotaru.re_mc.deathreturn.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ReturnNetwork {
    private static final String VERSION = "4";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("re_mc", "death_return"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals
    );

    private ReturnNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                0,
                ReturnTransitionPacket.class,
                ReturnTransitionPacket::encode,
                ReturnTransitionPacket::decode,
                ReturnTransitionPacket::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                1,
                SpiritSyncPacket.class,
                SpiritSyncPacket::encode,
                SpiritSyncPacket::decode,
                SpiritSyncPacket::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                2,
                GospelDataPacket.class,
                GospelDataPacket::encode,
                GospelDataPacket::decode,
                GospelDataPacket::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                3,
                WitchCursePacket.class,
                WitchCursePacket::encode,
                WitchCursePacket::decode,
                WitchCursePacket::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                4,
                UnseenHandActivatePacket.class,
                UnseenHandActivatePacket::encode,
                UnseenHandActivatePacket::decode,
                UnseenHandActivatePacket::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                5,
                UnseenHandVisualPacket.class,
                UnseenHandVisualPacket::encode,
                UnseenHandVisualPacket::decode,
                UnseenHandVisualPacket::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                6,
                UnseenHandCooldownPacket.class,
                UnseenHandCooldownPacket::encode,
                UnseenHandCooldownPacket::decode,
                UnseenHandCooldownPacket::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }
}
