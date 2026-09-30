package org.hotaru.re_mc.deathreturn.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ReturnNetwork {
    private static final String VERSION = "1";
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
    }
}
