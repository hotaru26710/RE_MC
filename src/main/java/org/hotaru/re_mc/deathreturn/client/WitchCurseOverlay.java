package org.hotaru.re_mc.deathreturn.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hotaru.re_mc.Re_mc;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WitchCurseOverlay {
    private static float darkness;
    private static boolean active;

    private WitchCurseOverlay() {
    }

    public static void set(float value, boolean enabled) {
        darkness = Math.max(0.0F, Math.min(1.0F, value));
        active = enabled && darkness > 0.001F;
    }

    public static void clear() {
        darkness = 0.0F;
        active = false;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (active) {
            ReturnTransitionScreen.renderWitchAura(event.getGuiGraphics(), darkness, 0.0F);
        }
    }
}
