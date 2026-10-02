package org.hotaru.re_mc.deathreturn.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hotaru.re_mc.Re_mc;
import org.hotaru.re_mc.deathreturn.network.ReturnNetwork;
import org.hotaru.re_mc.deathreturn.network.UnseenHandActivatePacket;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class UnseenHandKeyHandler {
    private UnseenHandKeyHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || minecraft.player == null || minecraft.screen != null) {
            return;
        }
        while (UnseenHandKeyMappings.ACTIVATE.consumeClick()) {
            ReturnNetwork.CHANNEL.sendToServer(new UnseenHandActivatePacket());
        }
    }
}