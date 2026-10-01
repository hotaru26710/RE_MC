package org.hotaru.re_mc.deathreturn.item;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import org.hotaru.re_mc.deathreturn.spirit.SpiritService;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public final class SpiritHerbItem extends Item {
    public SpiritHerbItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            SpiritService.restoreSpirit(serverPlayer, 8.0F, 6.0F);
            player.getCooldowns().addCooldown(this, 40);
            stack.shrink(1);
        }
        return InteractionResultHolder.consume(stack);
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.re_mc.spirit_herb").withStyle(ChatFormatting.GRAY));
    }
}
