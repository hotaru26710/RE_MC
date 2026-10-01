package org.hotaru.re_mc.deathreturn.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.hotaru.re_mc.deathreturn.memory.PlayerMemory;
import org.hotaru.re_mc.deathreturn.memory.ReturnMemoryService;
import org.hotaru.re_mc.deathreturn.network.GospelDataPacket;
import org.hotaru.re_mc.deathreturn.network.ReturnNetwork;

import javax.annotation.Nullable;
import java.util.List;

public final class GospelItem extends Item {
    public GospelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            PlayerMemory memory = ReturnMemoryService.getOrCreate(serverPlayer.server, serverPlayer.getUUID());
            ReturnNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new GospelDataPacket(
                            memory.getSpirit(),
                            memory.getWitchScent(),
                            memory.getTotalReturns(),
                            memory.getLastCheckpointTime(),
                            memory.getLastCheckpointDimension(),
                            memory.getDeaths().stream().limit(10).toList()
                    )
            );
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.re_mc.gospel").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
