package org.hotaru.re_mc.deathreturn.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hotaru.re_mc.Re_mc;
import org.hotaru.re_mc.deathreturn.mode.SubaruModePending;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SubaruModeScreenHook {
    private SubaruModeScreenHook() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof CreateWorldScreen screen)) {
            return;
        }
        if (SubaruModePending.screen() != screen) {
            SubaruModePending.reset(screen);
        }
        Button button = Button.builder(label(), pressed -> {
            boolean next = !SubaruModePending.isPending();
            SubaruModePending.setPending(next);
            if (next) {
                screen.getUiState().setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL);
            }
            pressed.setMessage(label());
        }).bounds(screen.width / 2 - 155, screen.height - 52, 150, 20).build();
        button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("screen.re_mc.subaru_mode.tooltip")));
        event.addListener(button);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Pre event) {
        if (SubaruModePending.isPending() && event.getScreen() instanceof CreateWorldScreen screen && screen.getUiState().isHardcore()) {
            screen.getUiState().setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL);
        }
    }
    private static Component label() {
        return Component.translatable("screen.re_mc.subaru_mode", SubaruModePending.isPending() ? Component.translatable("options.on") : Component.translatable("options.off"));
    }
}
