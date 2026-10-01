package org.hotaru.re_mc.deathreturn.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.hotaru.re_mc.deathreturn.memory.DeathRecord;
import org.hotaru.re_mc.deathreturn.network.GospelDataPacket;

import java.util.Locale;

public final class GospelScreen extends Screen {
    private final GospelDataPacket data;

    private GospelScreen(GospelDataPacket data) {
        super(Component.translatable("screen.re_mc.gospel.title"));
        this.data = data;
    }

    public static void open(GospelDataPacket data) {
        Minecraft.getInstance().setScreen(new GospelScreen(data));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int width = this.width;
        int height = this.height;
        graphics.fill(0, 0, width, height, 0xAA000000);
        int left = width / 2 - 150;
        int top = height / 2 - 105;
        int right = width / 2 + 150;
        int bottom = height / 2 + 105;
        graphics.fill(left - 2, top - 2, right + 2, top + 14, 0xFF0B0913);
        graphics.fill(left, top, right, bottom, 0xE61A1426);
        graphics.fill(left, top, right, top + 1, 0xFF6D4CFF);
        graphics.drawString(font, title, left + 8, top + 6, 0xC77CFF, false);
        graphics.drawString(font, Component.translatable("screen.re_mc.gospel.spirit", String.format(Locale.ROOT, "%.0f", data.spirit())), left + 8, top + 24, 0x4FF5FF, false);
        graphics.drawString(font, Component.translatable("screen.re_mc.gospel.scent", String.format(Locale.ROOT, "%.0f", data.witchScent())), left + 8, top + 38, 0xC77CFF, false);
        graphics.drawString(font, Component.translatable("screen.re_mc.gospel.returns", data.totalReturns()), left + 8, top + 52, 0xFF6BB6, false);
        graphics.drawString(font, Component.translatable("screen.re_mc.gospel.checkpoint", data.lastCheckpointDimension(), data.lastCheckpointTime()), left + 8, top + 66, 0xFFFFFF, false);
        graphics.fill(left + 8, top + 80, right - 8, top + 81, 0x664A3B70);
        graphics.drawString(font, Component.translatable("screen.re_mc.gospel.deaths"), left + 8, top + 88, 0xFFFFFF, false);

        int y = top + 104;
        for (DeathRecord record : data.deaths()) {
            String line = "#" + record.getDeathCount() + " " + record.getDimension() + " " + record.getCause();
            if (line.length() > 48) {
                line = line.substring(0, 45) + "...";
            }
            graphics.drawString(font, line, left + 12, y, 0xB9A9D8, false);
            y += 12;
        }
        if (data.deaths().isEmpty()) {
            graphics.drawString(font, Component.translatable("screen.re_mc.gospel.no_deaths"), left + 12, y, 0x887D9A, false);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
